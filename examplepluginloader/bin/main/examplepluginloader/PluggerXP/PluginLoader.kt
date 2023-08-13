package examplepluginloader.PluggerXP
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.net.URI
import java.util.UUID
import java.nio.file.Path
object PluginLoader {
    private val pluginClassMap = mutableMapOf<UUID,KClass<out MyPlugin>>() //<-- initialize our lists of stuff for loading and closing
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val cLoaderMap = mutableMapOf<UUID,URLClassLoader>() //<-- we will close these to unload plugins
    private val pluginLocation = mutableMapOf<UUID,URL>() //<-- this one is just for the user to reference.
    private val plugIDList = mutableListOf<UUID>()
    //public getter functions
    fun getPlugIDList(): List<UUID> = plugIDList.toList() //<-- return a copy of the List rather than the List itself to prevent concurrent modification exception
    fun getPluginMap(): Map<UUID, MyPlugin> = pluginObjectMap.toMap() //<-- return a copy of the Map rather than the Map itself to prevent concurrent modification exception
    fun getPlugin(plugID: UUID): MyPlugin? = pluginObjectMap[plugID]
    fun getPluginLocation(plugID: UUID): URL? = pluginLocation[plugID]
    fun getPluginUUID(plugin: MyPlugin): UUID? = pluginObjectMap.entries.find { it.value == plugin }?.key
    //unload and load functions (Synchronized)
    private val lock = Any() // Shared lock object
    @Synchronized
    fun unloadPlugins(plugIDs: List<UUID>) = plugIDs.forEach { plugID -> unloadPlugin(plugID) }
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
    @Synchronized
    fun loadPlugins(api: MyAPI, pluginPathStrings: List<String>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginLocations = mutableListOf<URI>()
        pluginPathStrings.forEach { pluginPathString -> 
            try{ pluginLocations.add(File(pluginPathString).toURI()) 
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginLocations, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromURLs(api: MyAPI, pluginURLs: List<URL>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginLocations = mutableListOf<URI>()
        pluginURLs.forEach { pluginURL -> 
            try{ pluginLocations.add(pluginURL.toURI())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginLocations, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromPaths(api: MyAPI, pluginPaths: List<Path>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginLocations = mutableListOf<URI>()
        pluginPaths.forEach { pluginPath -> 
            try{ pluginLocations.add(pluginPath.toUri())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginLocations, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromFiles(api: MyAPI, pluginFiles: List<File>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginLocations = mutableListOf<URI>()
        pluginFiles.forEach { pluginFile -> 
            try{ pluginLocations.add(pluginFile.toURI())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginLocations, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromURIs(api: MyAPI, pluginURIs: List<URI>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        return callPlugLoader(api, pluginURIs, targetPluginFullClassNames)
    }

    //End of public functions
    //main load class function
    private fun callPlugLoader(api: MyAPI, pluginURIs: List<URI>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginUUIDs = mutableListOf<UUID>()
        val pluginsToRemove = mutableListOf<UUID>()
        for(pluginURI in pluginURIs){
            val plugIDs = loadPluginsFromGenLocation(pluginURI, targetPluginFullClassNames) //<-- loadPluginsFromGenLocation(pluginURI: URI, targetClassNames: List<String>): MutableList<UUID>
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
            pluginUUIDs.remove(plugID) //<-- remember: remove the ones that
            unloadPlugin(plugID) //<-- dont load/crash on start to give accurate info
        }
        return pluginUUIDs.toList() //<-- returns a copy of the list of uuids of the new plugins ACTUALLY loaded
    }
    //private helper function for callPlugLoader(api: MyAPI, pluginPath: String): List<UUID>
    private fun loadPluginsFromGenLocation(pluginURI: URI, targetClassNames: List<String>): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        try{
            for(plugURL in getJarURLs(pluginURI)){ //<-- getJarURLs(pluginPath: File): List<URL>
                if(plugURL.protocol == "file")plugIDs.addAll(loadPluginsFromFileLocation(plugURL, targetClassNames))
            }
        }catch(e: Exception){e.printStackTrace()}
        return plugIDs //<-- returns the uuids of the new plugins loaded
    }
    private fun getJarURLs(pluginPathURI: URI): List<URL> {
        val pluginPath: URL
        pluginPath = pluginPathURI.toURL()
        if(pluginPath.protocol == "file"){
            if(File(pluginPathURI).isDirectory()){
                // get all jar files in the directory and convert list to a mutable list so we can add any .class files, then add those too
                val bytecodefiles = (File(pluginPathURI).listFiles { file -> file.name.endsWith(".jar") }
                    .map { it.toURI().toURL() }).toMutableList()
                bytecodefiles.addAll(File(pluginPathURI).listFiles { file -> file.name.endsWith(".class") }.map { it.toURI().toURL() })
                return bytecodefiles
            } else return listOf(pluginPath) //<-- else if specific file was specified, return the url as a 1 element list
        } else return listOf()
    }
    private fun loadPluginsFromFileLocation(plugURL: URL, targetClassNames: List<String>): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        // Get all subtypes of MyPlugin using Reflections (Which I cant get to work over the internet)
        val reflections = Reflections(ConfigurationBuilder().addUrls(plugURL)
            .addClassLoaders(URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader)))
        var pluginClasses = reflections.getSubTypesOf(MyPlugin::class.java).toList()
        //now that we have the classes, convert them to KClasses, and filter then load them
        plugIDs.addAll(loadPluginKClassesFromURLFilePath(pluginClasses.map { it.kotlin }, plugURL, targetClassNames))
        return plugIDs
    }
    //Once you finally have the KClass objects
    private fun loadPluginKClassesFromURLFilePath(pluginKClasses: List<KClass<out MyPlugin>>, plugURL: URL, targetClassNames: List<String>): MutableList<UUID> {
        var pluginClasses = pluginKClasses
        if(!targetClassNames.isEmpty()) pluginClasses = pluginClasses.filter { pluginClass ->
            targetClassNames.any { target -> ((pluginClass.qualifiedName == target)||(pluginClass.simpleName == target)) } //<-- If there were targetClassNames, filter for them
        }
        val plugIDs = mutableListOf<UUID>()
        pluginClasses.forEach { pluginClass ->
            val plugID = loadPlugin(URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader), pluginClass, plugURL) //<-- loadPlugin(cLoader, pluginClass, plugURL): UUID? defined below
            if(plugID!=null)plugIDs.add(plugID)//<-- add uuid to the newly-loaded uuid list
        }
        return plugIDs
    }
    private fun loadPlugin(cLoader: URLClassLoader, pluginClass: KClass<out MyPlugin>, plugURL: URL): UUID? {
        var launchableName = pluginClass.qualifiedName //<-- get class name
        if(launchableName==null)launchableName=pluginClass.simpleName //<-- if not in package it may only have simpleName
        if(launchableName!=null){ // if it has a name at all, launch it and update lists
            val pluginInstance = cLoader.loadClass(launchableName).getConstructor().newInstance() as MyPlugin
            val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
            pluginClassMap[pluginUUID] = pluginClass //add stuff into respective maps using UUID as the key
            pluginObjectMap[pluginUUID] = pluginInstance
            cLoaderMap[pluginUUID] = cLoader //<-- we keep track of cLoaders so we can close them later
            pluginLocation[pluginUUID] = plugURL
            plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
            return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
        } else return null
    }
}