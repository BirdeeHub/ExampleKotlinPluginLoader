package examplepluginloader.Plugger

import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import examplepluginloader.api.plugin.PluginUnloadHandler
import examplepluginloader.Plugger.JByteCodeURLINFO
import examplepluginloader.apimps.UnloadPlugistration
import examplepluginloader.apimps.MyAPIobj
import java.io.InputStream
import java.io.File
import java.net.URL
import java.net.URI
import java.util.UUID
import java.util.WeakHashMap
import java.nio.file.Path

//Singleton class, for easy referencing, and preventing duplication of references.
object PluginManager {
    //PRIVATE GLOBALS
    private val plugIDList = mutableListOf<UUID>() //<-- initialize our lists of stuff for loading and closing
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val pluginCLMap: WeakHashMap<UUID,PluginLoader> = WeakHashMap<UUID,PluginLoader>() //<-- we will close these to unload plugins
    private val classInfoByURLs = mutableMapOf<URL,List<JByteCodeURLINFO.URLclassInfo>>() //<-- I made a reflections with ASM that works over web, which shouldnt hold any references
    private val pluginAPIobjs = mutableMapOf<UUID,MyAPI>() //<-- these have the references of each api object i pass to a plugin
    private val shutdownRegistrations = mutableListOf<UnloadPlugistration>() //<-- These have a reference to a plugin defined shutdown handler

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
    fun getPluginCNamesAtURL(plugURL: URL): List<String>? =
        classInfoByURLs[plugURL]?.filter { it.optUUID!=null }
            ?.mapNotNull { it.name }?.map { JByteCodeURLINFO.getExtClassName(it) }
    fun getPluginClassName(plugID: UUID): String? { 
        val name = mutableListOf<String>()
        classInfoByURLs.forEach {
            name.addAll(it.value.filter { it.optUUID == plugID }.mapNotNull {it.name})
        }
        return if(name.size != 1) null else JByteCodeURLINFO.getExtClassName(name.get(0))
    }
    fun getPluginLocation(plugID: UUID): URL? = try{ 
        //only ever 1 url per uuid. 2 uuids for 1 url is possible but not relevant here, 
        //get(0) will throw error if UUID not found because list will be empty
        classInfoByURLs.filter { 
            it.value.any { it.optUUID == plugID }
        }.map { it.key }.get(0) 
    }catch(e: Exception){null}

    //Synchronized Public Functions
    private val lock = Any() // Shared lock object

    //If you wish to dangerCheck URL, get JByteCodeURLINFO and run the danger check
    //then put class info into here so you dont have to download it again
    @Synchronized
    fun addInfoCacheForURL(pluginClassInfo: List<JByteCodeURLINFO.URLclassInfo>){
        val info = pluginClassInfo.filter{ 
            it.isImpOf(JByteCodeURLINFO.getInternalCName(MyPlugin::class.java)) 
        }
        if(!info.isEmpty()) classInfoByURLs[info.get(0).urURL] = info
    }
    @Synchronized
    fun clearInfoCacheForURL(pluginURL: URL) = classInfoByURLs.remove(pluginURL)
    
    //Unload Functions
    @Synchronized
    fun unloadPlugin(plugID: UUID){ //close and remove EVERYWHERE
        shutdownRegistrations.forEach {
            if(it.plugID==plugID){ 
                try{ it.unldHndlr?.pluginUnloaded() 
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
        //no, this doesnt entirely work if you only GC 1 time...
        System.gc()
        try{
            Thread.sleep(250)
        }catch(e: InterruptedException){}
        System.gc()
        try{
            Thread.sleep(250)
        }catch(e: InterruptedException){}
    }
    @Synchronized
    fun unloadPlugins(plugIDs: List<UUID>){ //close and remove EVERYWHERE
        shutdownRegistrations.forEach {
            if(plugIDs.contains(it.plugID)){ 
                try{ it.unldHndlr?.pluginUnloaded() 
                }catch(e: Exception){e.printStackTrace()}
            }
        }
        val iterator = shutdownRegistrations.iterator()
        while (iterator.hasNext()) {
            val reg = iterator.next()
            if (plugIDs.contains(reg.plugID)) {
                iterator.remove()
            }
        }
        plugIDs.forEach { plugID ->
            pluginAPIobjs.remove(plugID)
            pluginObjectMap.remove(plugID)
            try{ pluginCLMap[plugID]?.close()
            }catch (e: Exception){e.printStackTrace()}
            pluginCLMap.remove(plugID) //these don't throw.
            plugIDList.remove(plugID)
        }
        //no, this doesnt entirely work if you only GC 1 time...
        System.gc()
        try{
            Thread.sleep(250)
        }catch(e: InterruptedException){}
        System.gc()
        try{
            Thread.sleep(500)
        }catch(e: InterruptedException){}
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        shutdownRegistrations.forEach { 
            try{ it.unldHndlr?.pluginUnloaded() 
            }catch(e: Exception){} 
        }
        shutdownRegistrations.clear()
        pluginAPIobjs.clear()
        pluginObjectMap.clear()
        pluginCLMap.forEach { try{ 
                it.value.close()
            }catch (e: Exception){e.printStackTrace()}
        }
        pluginCLMap.clear() //these don't throw.
        plugIDList.clear()
        //no, this doesnt entirely work if you only GC 1 time...
        System.gc()
        try{
            Thread.sleep(250)
        }catch(e: InterruptedException){}
        System.gc()
        try{
            Thread.sleep(500)
        }catch(e: InterruptedException){}
    }

    //Public Load Functions
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
            System.gc()
            Thread.sleep(250)
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
                try{ // Step 2: get Plugin info at each url with JByteCodeURLINFO
                    //eventually, we can optionally pre-populate JByteCodeURLINFO.classInfoAtURL 
                    //with a separate function so we can optionally virus scan before we load
                    if(classInfoByURLs[plugURL] == null){
                        classInfoByURLs[plugURL]=JByteCodeURLINFO(plugURL).classInfoAtURL.filter{ 
                            it.isImpOf(JByteCodeURLINFO.getInternalCName(MyPlugin::class.java)) 
                        }
                    }
                    //Step 3: map to name and add to the list to pass to loadPluginClasses
                    val pluginNames = classInfoByURLs[plugURL]?.mapNotNull {it.name}
                    // Step 4: create plugin instances and populate the globals at the top of file
                    if(pluginNames!=null)plugIDs.addAll(loadPluginClasses(plugURL, pluginNames, targetCNames))
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
        if(targetCNames.isEmpty()||(targetCNames.any { target -> (JByteCodeURLINFO.getExtClassName(pluginName) == target) })){
            //then get instance, UUID, then update global list/maps. Return new UUID so user knows which were 
            var loader: PluginLoader? = null
            try{
                val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
                loader = PluginLoader(plugURL, pluginUUID, PluginManager::class.java.classLoader.parent) //PluginManager is running under MyProgramLoader. Get parent, which is MySystemLoader
                val pluginInstance = loader.loadClass(JByteCodeURLINFO.getExtClassName(pluginName)).getConstructor().newInstance() as MyPlugin //<-- this will throw if theres a name collision in jar
                classInfoByURLs[plugURL]?.find { it.name==pluginName }?.optUUID = pluginUUID
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