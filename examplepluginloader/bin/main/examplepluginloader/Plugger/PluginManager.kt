package examplepluginloader.Plugger

import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import examplepluginloader.api.plugin.PluginUnloadHandler
import examplepluginloader.Plugger.JByteCodeURLINFO
import java.io.InputStream
import java.io.File
import java.net.URL
import java.net.URI
import java.util.UUID
import java.nio.file.Path


object PluginManager {
    //PRIVATE GLOBALS
    private val plugIDList = mutableListOf<UUID>() //<-- initialize our lists of stuff for loading and closing
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val pluginCLMap = mutableMapOf<UUID,PluginLoader>() //<-- we will close these to unload plugins
    private val classInfoByURLs = mutableMapOf<URL,JByteCodeURLINFO>() //<-- I made a reflections with ASM that works over web
    private val pluginAPIobjs = mutableMapOf<UUID,MyAPI>()
    private val shutdownRegistrations = mutableListOf<UnloadPlugistration>()

    //shutdown hook management functions
    fun registerShutdownHook(plugID: UUID, unldHndlr: PluginUnloadHandler): UnloadPlugistration {
        val reg = UnloadPlugistration(plugID, unldHndlr)
        shutdownRegistrations.add(reg)
        return reg
    }
    fun shudownRegistered(registration: UnloadPlugistration): Boolean = shutdownRegistrations.contains(registration)
    fun shutdownderegister(registration: UnloadPlugistration) = shutdownRegistrations.remove(registration)
    //public getter functions
    fun getPlugIDList(): List<UUID> = plugIDList.toList() //<-- return a copy of the List rather than the List itself to prevent concurrent modification exception
    fun getPlugin(plugID: UUID): MyPlugin? = pluginObjectMap[plugID]
    fun getPluginUUID(plugin: MyPlugin): UUID? = pluginObjectMap.entries.find { it.value == plugin }?.key
    fun getPluginAPIobj(plugID: UUID): MyAPI? = pluginAPIobjs[plugID]
    fun getPluginClassName(plugID: UUID): String? { 
        val namesmatchingUUID = mutableListOf<String?>()
        classInfoByURLs.mapNotNull { it.value.classInfoAtURL }.forEach {
            it.forEach { 
                if(it.optUUID==plugID) namesmatchingUUID.add(it.name)
            }
        }
        if(namesmatchingUUID.isEmpty())return null
        else if(namesmatchingUUID.size>1)return null //<-- this should never be able to happen. UUID is placed after creating instance, which would error for this
        else return namesmatchingUUID.get(0)
    }
    fun getPluginLocation(plugID: UUID): URL? = try{ 
        //only ever 1 url per uuid. 2 uuids for 1 url is possible but not relevant, 
        //get(0) will throw error if UUID not found because list will be empty
        classInfoByURLs.filter { it.value.classInfoAtURL
            ?.any { it.optUUID == plugID } ?: false 
        }.map { it.key }.get(0) 
    }catch(e: Exception){null}

    // for when you changed the name of the class that implements MyPlugin without restarting the program
    // we have it keep the cache on remove because there may be more than 1 plugin per url, and also
    //if we hang onto it, on reloads we can avoid doing the first of 2 downloads where we check for class names to run
    fun clearInfoCacheForURL(pluginURL: URL) = classInfoByURLs.remove(pluginURL)

    //public unload and load functions (Synchronized)
    private val lock = Any() // Shared lock object
    @Synchronized
    fun unloadPlugins(plugIDs: List<UUID>) = plugIDs.forEach { plugID -> unloadPlugin(plugID) }
    @Synchronized
    fun unloadPlugin(plugID: UUID){ //close and remove EVERYWHERE
        shutdownRegistrations.forEach {
            if(it.plugID==plugID){ try{ 
                    it.unldHndlr.pluginUnloaded() 
                }catch(e: Exception){e.printStackTrace()}
            }
        }
        val iterator = shutdownRegistrations.iterator()
        while (iterator.hasNext()) {
            val reg = iterator.next()
            if (reg.plugID == plugID) {
                iterator.remove()
            }
        }
        pluginAPIobjs.remove(plugID)
        pluginObjectMap.remove(plugID)
        try{ pluginCLMap[plugID]?.close()
        }catch (e: Exception){e.printStackTrace()}
        pluginCLMap.remove(plugID) //these don't throw.
        plugIDList.remove(plugID)
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        shutdownRegistrations.forEach { try{ it.unldHndlr.pluginUnloaded() }catch(e: Exception){} }
        shutdownRegistrations.clear()
        pluginAPIobjs.clear()
        pluginObjectMap.clear()
        pluginCLMap.forEach { try{ 
            it.value.close()
            }catch (e: Exception){e.printStackTrace()}
        }
        pluginCLMap.clear() //these don't throw.
        plugIDList.clear()
    }
    @Synchronized
    fun loadPluginFile(pluginPathStrings: List<String>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginPathStrings.forEach { pluginPathString -> 
            try{ pluginURIs.add(File(pluginPathString).toURI()) 
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(pluginURIs, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromURLs(pluginURLs: List<URL>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginURLs.forEach { pluginURL -> 
            try{ pluginURIs.add(pluginURL.toURI())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(pluginURIs, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromPaths(pluginPaths: List<Path>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginPaths.forEach { pluginPath -> 
            try{ pluginURIs.add(pluginPath.toUri())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(pluginURIs, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromFiles(pluginFiles: List<File>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginURIs = mutableListOf<URI>()
        pluginFiles.forEach { pluginFile -> 
            try{ pluginURIs.add(pluginFile.toURI())
            } catch(e: Exception) { e.printStackTrace() }
        }
        return callPlugLoader(pluginURIs, targetPluginFullClassNames)
    }
    @Synchronized
    fun loadPluginsFromURIs(pluginURIs: List<URI>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> = 
        callPlugLoader(pluginURIs, targetPluginFullClassNames)

//------------------------------------INTERNAL---------------------------INTERNAL---------------------------INTERNAL--------------------------------------------------

    //End of public functions
    //main load class function
    private fun callPlugLoader(pluginURIs: List<URI>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginUUIDs = mutableListOf<UUID>()
        val pluginsToRemove = mutableListOf<UUID>()
        for(pluginURI in pluginURIs){ //for each, call loadPluginsFromOneURI(pluginURI: URI, targetClassNames: List<String>): MutableList<UUID>
            val plugIDs = loadPluginsFromOneURI(pluginURI, targetPluginFullClassNames)//<-- loads the stuff, then we run launchPlugin(api) next
            pluginUUIDs.addAll(plugIDs) //<-- add new IDs to list
            for (plugID in plugIDs) { //<-- our list from loadPluginsFromOneURI
                try {
                    val api = pluginAPIobjs[plugID]
                    if(api!=null){
                        val pluginInstance = pluginObjectMap[plugID]
                        if(pluginInstance!=null)pluginInstance.launchPlugin(api) //<-- launchplugin(api) must be defined when you implement MyPlugin
                        else pluginsToRemove.add(plugID) //<-- if not launchable, 
                    } else pluginsToRemove.add(plugID) //<-- add to separate list for deletion
                } catch (e: Exception) {
                    e.printStackTrace()
                    pluginsToRemove.add(plugID) //so that we arent modifying our collection while iterating over it
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
    private fun loadPluginsFromOneURI(pluginURI: URI, targetCNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        try{ // Step 1: getJarURLs(pluginPath: URI): List<URL>
            getJarURLs(pluginURI).forEach { plugURL ->
                val pluginNames = mutableListOf<String>()
                try{ // Step 2: get Class info at each url with JByteCodeURLINFO
                            //WITHOUT LOADING THEM
                    if(classInfoByURLs[plugURL] == null)
                        classInfoByURLs[plugURL]=JByteCodeURLINFO(plugURL)
                    //Step 3: filter out instances of our plugin and add to names list
                    classInfoByURLs[plugURL]?.classInfoAtURL?.filter{ 
                            it.isImpOf(JByteCodeURLINFO.getInternalCName(MyPlugin::class.java)) 
                        }?.mapNotNull {it.name}?.forEach { name -> 
                            pluginNames.add(JByteCodeURLINFO.getExtClassName(name))
                        }
                    // Step 4: create plugin instances and populate the globals at the top of file
                    plugIDs.addAll(loadPluginClasses(plugURL, pluginNames, targetCNames))
                }catch(e: Exception){e.printStackTrace()}
            }
        }catch(e: Exception){e.printStackTrace()}
        return plugIDs //<-- returns the uuids of the new plugins loaded to be launched by call plug loader
    }

    //gets URLS of bytecode files in directories, or URI as URL
    private fun getJarURLs(pluginPathURI: URI): List<URL> {
        try{
            val pluginPath: URL
            pluginPath = pluginPathURI.toURL()
            // if file:
            if(pluginPath.protocol == "file"){//<-- is file?
                if((File(pluginPathURI)).exists()){//<-- exists?
                    val pluginFile = File(pluginPathURI)
                    if(pluginFile.isDirectory()){//<--  is directory?
                        // get all jar files in the directory and convert list to a mutable list so we can add any .class files, then add those too
                        val bytecodefiles = (pluginFile.listFiles { file -> file.name.endsWith(".jar") }
                            .map { it.toURI().toURL() }).toMutableList()
                        bytecodefiles.addAll(pluginFile.listFiles { file -> file.name.endsWith(".class") }
                            .map { it.toURI().toURL() })
                        return bytecodefiles //<-- return our list of bytecode files in directory as urls
                    } else return listOf(pluginPath) //<-- else if specific file was specified, return the url as a 1 element list
                } else return listOf()
            // else if web http/s:
            } else if(pluginPath.protocol == "http" || pluginPath.protocol == "https" && 
                (pluginPath.toString().endsWith(".jar")||pluginPath.toString().endsWith(".class"))) {
                return listOf(pluginPath) //<-- you have to specify the whole URL
        // Otherwise: return empty list:
            } else return listOf()
        }catch(e: Exception){ e.printStackTrace(); return listOf() }
    }

    //Once you finally have the Class objects
    private fun loadPluginClasses(plugURL: URL, pluginNames: List<String>, targetCNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        pluginNames.forEach { pluginName -> // use copy of loader for each plugin to allow individual closing
            val plugID = loadPluginClass(plugURL, pluginName, targetCNames)
            if(plugID!=null)plugIDs.add(plugID)//<-- if it worked, add uuid to the newly-loaded uuid list
        }
        return plugIDs //<-- return list of new plugins to be launched by callPlugLoader (returns to loadPluginsFromOneURI. End of "Step 4")
    }
    private fun loadPluginClass(plugURL: URL, pluginName: String, targetCNames: List<String> = listOf()): UUID? {
        //first, check our targets list
        if(targetCNames.isEmpty()||(targetCNames.any { target -> ((pluginName == target)) })){
            //then get instance, UUID, then update global list/maps. Return new UUID so user knows which were 
            var loader: PluginLoader? = null
            try{
                val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
                loader = PluginLoader(plugURL, pluginUUID, PluginManager::class.java.classLoader.parent) //PluginManager is running under MyProgramLoader. Get parent, which is MySystemLoader
                val pluginInstance = loader.loadClass(pluginName).getConstructor().newInstance() as MyPlugin //<-- this will throw if theres a name collision in VM
                classInfoByURLs[plugURL]?.getClassInfoByExtName(pluginName)?.optUUID = pluginUUID //<-- getClassInfoByName throws if name collision at url
                pluginAPIobjs[pluginUUID] = MyAPIobj(pluginUUID) //<-- create a new api obj for each one so that we can manage them individually
                pluginObjectMap[pluginUUID] = pluginInstance
                pluginCLMap[pluginUUID] = loader //<-- we keep track of PluginLoaders so we can close them later
                plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
                return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
            }catch(e: Exception){
                e.printStackTrace()
                loader?.close() //<-- nothing else to remove because it throws first
                return null
            }
        } else return null
    }
}