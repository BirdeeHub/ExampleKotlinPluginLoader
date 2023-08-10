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
object PluginLoader {
    private val pluginClassMap: MutableMap<UUID,KClass<out MyPlugin>> = mutableMapOf()
    private val pluginObjectMap: MutableMap<UUID,MyPlugin> = mutableMapOf()
    private val cLoaderMap: MutableMap<UUID,URLClassLoader> = mutableMapOf()
    private val plugIDList: MutableList<UUID> = mutableListOf()
    //public functions
    fun getPlugIDList(): List<UUID> { return plugIDList }
    fun getPluginMap(): Map<UUID, MyPlugin> { return pluginObjectMap }
    fun getPlugin(plugID: UUID): MyPlugin? { return pluginObjectMap[plugID] }
    fun unloadPlugin(plugID: UUID){
        cLoaderMap[plugID]?.close() //close and remove EVERYWHERE
        cLoaderMap.remove(plugID)
        pluginClassMap.remove(plugID)
        pluginObjectMap.remove(plugID)
        plugIDList.remove(plugID) // then remove uuid from list
    }
    fun unloadAllPlugins() {
        for(entry in cLoaderMap)entry.value.close() //close and clear ALL everywhere
        cLoaderMap.clear()
        pluginClassMap.clear()
        pluginObjectMap.clear()
        plugIDList.clear() //then clear uuids
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
    //helper functions for callPlugLoader(api: MyAPI, pluginPath: String): List<UUID>
    private fun loadPlugins(pluginPath: File): MutableList<UUID> {
        val plugIDs: MutableList<UUID> = mutableListOf()
        val jarURLs = getJarURLs(pluginPath)
        for(entry in jarURLs){
            //create a list for reflections to put what it finds into
            val plugin: MutableList<KClass<out MyPlugin>> = mutableListOf()
            //create a classloader for finding and loading classes
            val cLoader: URLClassLoader = URLClassLoader(arrayOf(entry), PluginLoader::class.java.classLoader)
            // Create a new Reflections instance without specifying the package name
            val reflections = Reflections(ConfigurationBuilder().addUrls(entry).addClassLoaders(cLoader))
            // Get all subtypes of MyPlugin using Reflections
            val pluginClasses = reflections.getSubTypesOf(MyPlugin::class.java)
            // Convert the pluginClasses set to a list of KClass objects
            plugin.addAll(pluginClasses.map { it.kotlin })
            for (pluginClass in plugin) {
                // Load and initialize each plugin class using the custom class loader
                val pluginInstance = loadPluginClass(cLoader, pluginClass)
                if (pluginInstance != null) {
                    val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
                    plugIDs.add(pluginUUID) //<-- add the uuid to the uuid list
                    pluginClassMap[pluginUUID] = pluginClass //add class, loaded instance, and class loader, 
                    pluginObjectMap[pluginUUID] = pluginInstance //into respective maps using UUID as the key
                    cLoaderMap[pluginUUID] = cLoader
                }
            }
        }
        plugIDList.addAll(plugIDs) //<-- add new uuids to the actual list
        return plugIDs //<-- returns the uuids of the new plugins loaded
    }
    //helper functions for loadPlugins(pluginPath: File): MutableList<UUID>
    private fun getJarURLs(pluginPath: File): List<URL> {
        if(pluginPath.isDirectory()){
            // get all jar files in the directory and convert list to a mutable list so we can add any .class files
            val bytecodefiles = (pluginPath.listFiles { file -> file.name.endsWith(".jar") }
                .map { it.toURI().toURL() }).toMutableList()
            //add any .class files in the directory
            bytecodefiles.addAll(pluginPath.listFiles { file -> file.name.endsWith(".class") }.map { it.toURI().toURL() })
            return bytecodefiles
        } else {//<-- else if specific file was specified, return the url as a 1 element list
            return listOf(pluginPath.toURI().toURL()) 
        }
    }
    private fun loadPluginClass(classLoader: ClassLoader, pluginClass: KClass<out MyPlugin>): MyPlugin? {
        try { //load the instance and try to cast it as MyPlugin
            return classLoader.loadClass(pluginClass.qualifiedName).getConstructor().newInstance() as? MyPlugin 
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
