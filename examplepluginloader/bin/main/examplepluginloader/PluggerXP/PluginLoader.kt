package examplepluginloader.PluggerXP

import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import examplepluginloader.PluggerXP.JByteCodeURLINFO
import java.io.InputStream
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.net.URI
import java.net.URLClassLoader
import java.util.UUID
import java.util.Enumeration
import java.nio.file.Path


object PluginLoader {
    //The custom class loader that has no parent, and knows where to find its dependency
    private class URLoader(val plugURL: URL): 
        URLClassLoader(arrayOf(plugURL, 
        File(MyPlugin::class.java.protectionDomain.codeSource.location.toURI()).toURI().toURL()),ClassLoader.getSystemClassLoader()) {

    }
    //PRIVATE GLOBALS
    private val plugIDList = mutableListOf<UUID>() //<-- initialize our lists of stuff for loading and closing
    private val pluginURLMap = mutableMapOf<UUID,URL>()
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val uRLoaderMap = mutableMapOf<UUID,URLoader>() //<-- we will close these to unload plugins
    private val pluginNameMap = mutableMapOf<UUID,String>() //<-- it felt like I should include this
    private val classInfoByURLs = mutableMapOf<URL,JByteCodeURLINFO>()

    //public getter functions
    fun getPlugIDList(): List<UUID> = plugIDList.toList() //<-- return a copy of the List rather than the List itself to prevent concurrent modification exception
    fun getPlugin(plugID: UUID): MyPlugin? = pluginObjectMap[plugID]
    fun getPluginUUID(plugin: MyPlugin): UUID? = pluginObjectMap.entries.find { it.value == plugin }?.key
    fun getPluginClassName(plugID: UUID): String? = pluginNameMap[plugID]
    fun getPluginLocation(plugID: UUID): URL? = pluginURLMap[plugID]

    //public unload and load functions (Synchronized)
    private val lock = Any() // Shared lock object
    @Synchronized
    fun unloadPlugins(plugIDs: List<UUID>) = plugIDs.forEach { plugID -> unloadPlugin(plugID) }
    @Synchronized
    fun unloadPlugin(plugID: UUID){ //close and remove EVERYWHERE
        pluginObjectMap.remove(plugID)
        try{ uRLoaderMap[plugID]?.close() //<-- if already closed somehow, this can throw
        }catch (e: Exception){e.printStackTrace()}
        uRLoaderMap.remove(plugID) //these don't throw.
        plugIDList.remove(plugID)
        pluginURLMap.remove(plugID)
        pluginNameMap.remove(plugID)
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        pluginObjectMap.clear()
        uRLoaderMap.forEach { loader -> try{ 
            loader.value.close() //<-- if already closed somehow, this can throw
            }catch (e: Exception){e.printStackTrace()}
        }
        uRLoaderMap.clear() //these don't throw.
        plugIDList.clear()
        pluginNameMap.clear()
        pluginURLMap.clear()
    }
    @Synchronized
    fun loadPluginFile(api: MyAPI, pluginPathStrings: List<String>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
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
    fun loadPluginsFromURIs(api: MyAPI, pluginURIs: List<URI>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> = 
        callPlugLoader(api, pluginURIs, targetPluginFullClassNames)

//------------------------------------INTERNAL---------------------------INTERNAL---------------------------INTERNAL--------------------------------------------------

    //End of public functions
    //main load class function
    private fun callPlugLoader(api: MyAPI, pluginURIs: List<URI>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> {
        val pluginUUIDs = mutableListOf<UUID>()
        val pluginsToRemove = mutableListOf<UUID>()
        for(pluginURI in pluginURIs){ //for each, call loadPluginsFromOneURI(pluginURI: URI, targetClassNames: List<String>): MutableList<UUID>
            val plugIDs = loadPluginsFromOneURI(pluginURI, targetPluginFullClassNames)//<-- loads the stuff, then we run launchPlugin(api) next
            pluginUUIDs.addAll(plugIDs) //<-- add new IDs to list
            for (plugID in plugIDs) { //<-- our list from loadPluginsFromOneURI
                try {
                    val pluginInstance = pluginObjectMap[plugID]
                    if(pluginInstance!=null)pluginInstance.launchPlugin(api) //<-- launchplugin(api) must be defined when you implement MyPlugin
                    else pluginsToRemove.add(plugID) //<-- if not launchable, 
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
    private fun loadPluginsFromOneURI(pluginURI: URI, targetCNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        try{ // Step 1: getJarURLs(pluginPath: URI): List<URL>
            getJarURLs(pluginURI).forEach { plugURL ->
                val pluginNames = mutableListOf<String>()
                try{ // Step 2: get Class info at each url with JByteCodeURLINFO
                            //WITHOUT LOADING THEM
                    classInfoByURLs[plugURL]=JByteCodeURLINFO(plugURL)
                    //Step 3: filter out instances of our plugin and add to names list
                    classInfoByURLs[plugURL]?.classInfoAtURL?.filter{ 
                        it.isImpOf(JByteCodeURLINFO.getInternalCName(MyPlugin::class.java)) 
                        }?.map {it.name}?.forEach { name -> 
                            if(name!=null)
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
            //then get instance, UUID, then update global list/maps. Return new UUID so user knows which were loaded
            val loader = URLoader(plugURL)
            val pluginInstance = loader.loadClass(pluginName).getConstructor().newInstance() as MyPlugin
            val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
            //add names to map by UUID
            pluginURLMap[pluginUUID] = plugURL
            pluginNameMap[pluginUUID] = pluginName
            pluginObjectMap[pluginUUID] = pluginInstance 
            uRLoaderMap[pluginUUID] = loader //<-- we keep track of uRLoaders so we can close them later
            plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
            return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
        } else return null
    }

}