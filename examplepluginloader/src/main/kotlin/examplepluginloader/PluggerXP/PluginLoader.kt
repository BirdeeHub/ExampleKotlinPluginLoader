package examplepluginloader.PluggerXP
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.io.path.toPath
import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.UUID
object PluginLoader {
    private val lock = Any() // Shared lock object
    private val pluginClassMap = mutableMapOf<UUID,KClass<out MyPlugin>>() //<-- initialize our lists of stuff for loading and closing
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val cLoaderMap = mutableMapOf<UUID,URLClassLoader>() //<-- we will close these to unload plugins
    private val pluginLocation = mutableMapOf<UUID,String>() //<-- this one is just for the user to reference. It is the file name
    private val plugIDList = mutableListOf<UUID>()
    //public functions
    fun getPlugIDList(): List<UUID> = plugIDList.toList() //<-- return a copy of the List rather than the List itself to prevent concurrent modification exception
    fun getPluginMap(): Map<UUID, MyPlugin> = pluginObjectMap.toMap() //<-- return a copy of the Map rather than the Map itself to prevent concurrent modification exception
    fun getPlugin(plugID: UUID): MyPlugin? = pluginObjectMap[plugID]
    fun getPluginLocation(plugID: UUID): String? = pluginLocation[plugID]
    fun getPluginUUID(plugin: MyPlugin): UUID? = pluginObjectMap.entries.find { it.value == plugin }?.key
    @Synchronized
    fun unloadPlugin(plugID: UUID){ //close and remove EVERYWHERE
        try{ cLoaderMap[plugID]?.close() //<-- if already closed somehow, this can throw
        }catch (e: Exception){e.printStackTrace()}
        cLoaderMap.remove(plugID) //these don't throw.
        pluginClassMap.remove(plugID)
        pluginObjectMap.remove(plugID)
        pluginLocation.remove(plugID)
        plugIDList.remove(plugID)
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        for(entry in cLoaderMap)try{ entry.value.close() //<-- if already closed somehow, this can throw
            }catch (e: Exception){e.printStackTrace()}
        cLoaderMap.clear() //these don't throw.
        pluginClassMap.clear()
        pluginObjectMap.clear()
        pluginLocation.clear()
        plugIDList.clear()
    }
    //public load class function
    @Synchronized
    fun callPlugLoader(api: MyAPI, pluginPaths: Array<String>, targetPluginFullClassNames: Array<String> = arrayOf()): List<UUID> {
        val pluginUUIDs = mutableListOf<UUID>()
        val pluginsToRemove = mutableListOf<UUID>()
        for(pluginPath in pluginPaths){
            val plugIDs = loadPlugins(File(pluginPath), targetPluginFullClassNames) //<-- loads plugins and returns list of UUIDs of loaded plugins
            pluginUUIDs.addAll(plugIDs)
            for (plugID in plugIDs) {
                try {
                    val pluginInstance = pluginObjectMap[plugID]
                    if(pluginInstance!=null)pluginInstance.launchPlugin(api) //<-- launchplugin(api) must be defined when you implement CLioSMapPlugin
                    else pluginsToRemove.add(plugID)
                } catch (e: Exception) {
                    e.printStackTrace()
                    pluginsToRemove.add(plugID) //<-- add to separate list so that we arent modifying our collection while iterating over it
                }
            }
        }
        pluginsToRemove.forEach { plugID ->
            pluginUUIDs.remove(plugID) //<-- remember:
            unloadPlugin(plugID) //<-- remove the ones that dont load/crash on start to give accurate info
        }
        return pluginUUIDs.toList() //<-- returns a copy of the list of uuids of the new plugins ACTUALLY loaded
    }
    //private helper function for callPlugLoader(api: MyAPI, pluginPath: String): List<UUID>
    private fun loadPlugins(pluginPath: File, targetClassNames: Array<String>): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        for(plugURL in getJarURLs(pluginPath)){ //getJarURLs(pluginPath: File): List<URL> defined at end
            //create a classloader for finding and loading classes
            var cLoader: URLClassLoader = URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader)
            val reflections = Reflections(ConfigurationBuilder().addUrls(plugURL).addClassLoaders(cLoader))
            // Get all subtypes of MyPlugin using Reflections
            var pluginClasses = reflections.getSubTypesOf(MyPlugin::class.java).toList()
            if(!targetClassNames.isEmpty()){// filter for target classes if any
                pluginClasses = pluginClasses.filter { pluginClass ->
                    targetClassNames.any { target -> pluginClass.name == target }
                }
            }
            var i = 0 //<-- we use this to check if we have multiple plugins and need more class loaders
            // Convert the pluginClasses to a list of KClass objects and loop over it
            pluginClasses.map { it.kotlin }.forEach { pluginClass ->
                // Create new class loader after 1st iteration if multiple plugins were in the jar file, to allow individual closing
                if(i++ != 0)cLoader=URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader)
                val plugID = loadPlugin(cLoader, pluginClass, plugURL) //<-- loadPlugin defined below
                if(plugID!=null)plugIDs.add(plugID)
            }
        }
        return plugIDs //<-- returns the uuids of the new plugins loaded
    }
    private fun loadPlugin(cLoader: URLClassLoader, pluginClass: KClass<out MyPlugin>, plugURL: URL): UUID? {
        var launchableName = pluginClass.qualifiedName //<-- get class name
        if(launchableName==null)launchableName=pluginClass.simpleName //<-- if not in package it may only have simpleName
        if(launchableName!=null){
            val pluginInstance = cLoader.loadClass(launchableName).getConstructor().newInstance() as MyPlugin
            val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
            pluginClassMap[pluginUUID] = pluginClass //add stuff into respective maps using UUID as the key
            pluginObjectMap[pluginUUID] = pluginInstance
            cLoaderMap[pluginUUID] = cLoader //<-- we keep track of cLoaders so we can close them later
            pluginLocation[pluginUUID] = plugURL.toURI().toPath().toString()
            plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
            return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
        }
        return null
    }
    private fun getJarURLs(pluginPath: File): List<URL> {
        if(pluginPath.isDirectory()){
            // get all jar files in the directory and convert list to a mutable list so we can add any .class files, then add those too
            val bytecodefiles = (pluginPath.listFiles { file -> file.name.endsWith(".jar") }
                .map { it.toURI().toURL() }).toMutableList()
            bytecodefiles.addAll(pluginPath.listFiles { file -> file.name.endsWith(".class") }.map { it.toURI().toURL() })
            return bytecodefiles
        } else return listOf(pluginPath.toURI().toURL()) //<-- else if specific file was specified, return the url as a 1 element list
    }
}