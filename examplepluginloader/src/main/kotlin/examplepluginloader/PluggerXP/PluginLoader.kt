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
    private val plugIDList = mutableListOf<UUID>()

    //public getter functions
    fun getPlugIDList(): List<UUID> = plugIDList.toList() //<-- return a copy of the List rather than the List itself to prevent concurrent modification exception
    fun getPluginMap(): Map<UUID, MyPlugin> = pluginObjectMap.toMap() //<-- return a copy of the Map rather than the Map itself to prevent concurrent modification exception
    fun getPlugin(plugID: UUID): MyPlugin? = pluginObjectMap[plugID]
    fun getPluginLocation(plugID: UUID): URL? = cLoaderMap[plugID]?.getURLs()?.get(0)//<-- each loader has only 1 class and 1 url anyway
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
        plugIDList.remove(plugID)
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        for(entry in cLoaderMap)try{ entry.value.close() //<-- if already closed somehow, this can throw
            }catch (e: Exception){e.printStackTrace()}
        cLoaderMap.clear() //these don't throw.
        pluginClassMap.clear()
        pluginObjectMap.clear()
        plugIDList.clear()
    }
    @Synchronized
    fun loadPlugins(api: MyAPI, pluginPathStrings: List<String>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginPathStrings.forEach { pluginPathString -> 
            try{ pluginURIs.add(File(pluginPathString).toURI()) 
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginURIs, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromURLs(api: MyAPI, pluginURLs: List<URL>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginURLs.forEach { pluginURL -> 
            try{ pluginURIs.add(pluginURL.toURI())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginURIs, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromPaths(api: MyAPI, pluginPaths: List<Path>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginPaths.forEach { pluginPath -> 
            try{ pluginURIs.add(pluginPath.toUri())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginURIs, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromFiles(api: MyAPI, pluginFiles: List<File>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginFiles.forEach { pluginFile -> 
            try{ pluginURIs.add(pluginFile.toURI())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(api, pluginURIs, targetPluginFullClassNames)
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
        for(pluginURI in pluginURIs){ //for each call loadPluginsFromGenLocation(pluginURI: URI, targetClassNames: List<String>): MutableList<UUID>
            val plugIDs = loadPluginsFromOneURI(pluginURI, targetPluginFullClassNames)
            pluginUUIDs.addAll(plugIDs)
            for (plugID in plugIDs) {
                try {
                    val pluginInstance = pluginObjectMap[plugID]
                    if(pluginInstance!=null)pluginInstance.launchPlugin(api) //<-- launchplugin(api) must be defined when you implement CLioSMapPlugin
                    else pluginsToRemove.add(plugID) // if not launchable, 
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
    private fun loadPluginsFromOneURI(pluginURI: URI, targetClassNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        try{
            getJarURLs(pluginURI).forEach { plugURL -> //<-- Step 1: getJarURLs(pluginPath: File): List<URL>
                //Step 2: get ClassLoader for single bytecode file and init list of plugin Class objects
                val cLoader = URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader)
                val pluginClasses = mutableListOf<Class<out MyPlugin>>()
                try{ //Step 2: get Class objects at each url with ClassLoader
                    if(plugURL.protocol == "file")pluginClasses.addAll(GetPluginsFromFile(plugURL, cLoader))
                    //Step 3: loadPluginClasses(List<Class<out MyPlugin>>, URLClassLoader, List<String>)
                    plugIDs.addAll(loadPluginClasses(pluginClasses, cLoader, targetClassNames))
                }catch(e: Exception){e.printStackTrace()}
            }
        }catch(e: Exception){e.printStackTrace()}
        return plugIDs //<-- returns the uuids of the new plugins loaded
    }
    //gets URLS of bytecode files in directories, or URI as URL
    private fun getJarURLs(pluginPathURI: URI): List<URL> {
        try{
            val pluginPath: URL
            pluginPath = pluginPathURI.toURL()
            if(pluginPath.protocol == "file"){
                if((File(pluginPathURI)).exists()){
                    val pluginFile = File(pluginPathURI)
                    if(pluginFile.isDirectory()){
                        // get all jar files in the directory and convert list to a mutable list so we can add any .class files, then add those too
                        val bytecodefiles = (pluginFile.listFiles { file -> file.name.endsWith(".jar") }
                            .map { it.toURI().toURL() }).toMutableList()
                        bytecodefiles.addAll(pluginFile.listFiles { file -> file.name.endsWith(".class") }.map { it.toURI().toURL() })
                        return bytecodefiles
                    } else return listOf(pluginPath) //<-- else if specific file was specified, return the url as a 1 element list
                } else return listOf()
            } else return listOf()
        }catch(e: Exception){ e.printStackTrace(); return listOf() }
    }

    // Get all subtypes of MyPlugin using Reflections (Which I cant get to work over the internet)
    private fun GetPluginsFromFile(plugURL: URL, cLoader: URLClassLoader): List<Class<out MyPlugin>> = Reflections(
        ConfigurationBuilder().addUrls(plugURL).addClassLoaders(cLoader)).getSubTypesOf(MyPlugin::class.java).toList()

    //Once you finally have the Class objects
    private fun loadPluginClasses(pluginClasses: List<Class<out MyPlugin>>, URLoader: URLClassLoader, targetClassNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        pluginClasses.forEach { pluginClass -> // use copy of loader to allow individual closing, also map to KClass
            val plugID = loadPlugin(URLClassLoader(URLoader.getName(), URLoader.getURLs(), URLoader.parent), pluginClass.kotlin, targetClassNames)
            if(plugID!=null)plugIDs.add(plugID)//<-- if it worked, add uuid to the newly-loaded uuid list
        }
        return plugIDs
    }
    private fun loadPlugin(cLoader: URLClassLoader, pluginClass: KClass<out MyPlugin>, targetClassNames: List<String> = listOf()): UUID? {
        if(targetClassNames.isEmpty() || (targetClassNames.any { target -> ((pluginClass.qualifiedName == target)||(pluginClass.simpleName == target)) })){
            var launchableName = pluginClass.qualifiedName //<-- get class name
            if(launchableName==null)launchableName=pluginClass.simpleName //<-- if not in package it may only have simpleName
            if(launchableName!=null){ // if it has a name at all, launch it and update lists
                val pluginInstance = cLoader.loadClass(launchableName).getConstructor().newInstance() as MyPlugin
                val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
                pluginClassMap[pluginUUID] = pluginClass //add stuff into respective maps using UUID as the key
                pluginObjectMap[pluginUUID] = pluginInstance
                cLoaderMap[pluginUUID] = cLoader //<-- we keep track of cLoaders so we can close them later
                plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
                return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
            } else return null
        } else return null
    }
}