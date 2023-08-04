package examplepluginloader.PluggerXP
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.UUID
class PluginLoader {
    companion object {
        private val pluginClassMap: MutableMap<UUID,KClass<out MyPlugin>> = mutableMapOf()
        private val pluginObjectMap: MutableMap<UUID,MyPlugin> = mutableMapOf()
        private val cLoaderMap: MutableMap<UUID,URLClassLoader> = mutableMapOf()
        private val plugIDList: MutableList<UUID> = mutableListOf()
        fun getPlugIDList(): List<UUID> { return plugIDList }
        fun getPluginMap(): Map<UUID, MyPlugin> { return pluginObjectMap }
        fun getPlugin(plugID: UUID): MyPlugin? { return pluginObjectMap[plugID] }
        fun unloadPlugin(plugID: UUID){
            cLoaderMap[plugID]?.close()
            cLoaderMap.remove(plugID)
            pluginClassMap.remove(plugID)
            pluginObjectMap.remove(plugID)
            plugIDList.remove(plugID)
        }
        fun unloadAllPlugins() {
            for(entry in cLoaderMap)entry.value.close()
            cLoaderMap.clear()
            pluginClassMap.clear()
            pluginObjectMap.clear()
            plugIDList.clear()
        }
        fun callPlugLoader(api: MyAPI, pluginPath: String): List<UUID> {
            val pluginUUIDs = loadPlugins(File(pluginPath))
            val pluginsToRemove = mutableListOf<UUID>()
            for (plugID in pluginUUIDs) {
                try {
                    val constructor = pluginClassMap[plugID]?.constructors?.first()
                    val pluginInstance = constructor?.call()
                    pluginInstance?.launchPlugin(api)//<-- launchplugin(api) must be defined when you implement MyPlugin
                } catch (e: Exception) {
                    e.printStackTrace()
                    pluginsToRemove.add(plugID) //<-- add to separate list so that we arent modifying our collection while iterating over it
                }
            }
            pluginsToRemove.forEach { plugID ->
                pluginUUIDs.remove(plugID) //<-- remember:
                unloadPlugin(plugID) //<-- remove the ones that dont load to give accurate info
            }
            return pluginUUIDs //<-- returns the uuids of the new plugins ACTUALLY loaded
        }
        //private functions
        private fun loadPlugins(pluginPath: File): MutableList<UUID> {
            val plugIDs: MutableList<UUID> = mutableListOf()
            val jarURLs = getJarURLs(pluginPath)
            for(entry in jarURLs){
                val plugin: MutableList<KClass<out MyPlugin>> = mutableListOf()
                val cLoader: URLClassLoader = URLClassLoader(arrayOf(entry), PluginLoader::class.java.classLoader)
                // Create a new Reflections instance without specifying the package name
                val reflections = Reflections(ConfigurationBuilder().addUrls(entry).addClassLoaders(cLoader))
                // Get all subtypes of MyPlugin using Reflections
                val pluginClasses = reflections.getSubTypesOf(MyPlugin::class.java)
                // Convert the pluginClasses set to a list of KClass objects
                plugin.addAll(pluginClasses.map { it.kotlin })
                // Load and initialize each plugin class using the custom class loader
                for (pluginClass in plugin) {
                    val pluginInstance = loadPluginClass(cLoader, pluginClass)
                    if (pluginInstance != null) {
                        val pluginUUID = UUID.randomUUID()
                        plugIDs.add(pluginUUID)
                        pluginClassMap[pluginUUID] = pluginClass
                        pluginObjectMap[pluginUUID] = pluginInstance
                        cLoaderMap[pluginUUID] = cLoader
                    }
                }
            }
            plugIDList.addAll(plugIDs)
            return plugIDs //<-- returns the uuids of the new plugins loaded
        }
        private fun getJarURLs(pluginPath: File): List<URL> {
            if(pluginPath.isDirectory()){// get all jar and class files in the directory
                val bytecodefiles = (pluginPath.listFiles { file -> file.name.endsWith(".jar") }.map { it.toURI().toURL() }).toMutableList()
                bytecodefiles.addAll(pluginPath.listFiles { file -> file.name.endsWith(".class") }.map { it.toURI().toURL() })
                return bytecodefiles
            } else {//<-- specific file was specified
                return listOf(pluginPath.toURI().toURL()) 
            }
        }
        private fun loadPluginClass(classLoader: ClassLoader, pluginClass: KClass<out MyPlugin>): MyPlugin? {
            try {
                val pluginInstance = classLoader.loadClass(pluginClass.qualifiedName)
                    .getConstructor().newInstance() as? MyPlugin
                return pluginInstance 
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}
