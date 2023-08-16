package examplepluginloader.PluggerXP

import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import org.objectweb.asm.Type //<-- these dependencies from here to:
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes //<-- here are used in the last function of the file to do magic (and remove need for reflections)
import java.io.InputStream
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.net.URL
import java.net.URLClassLoader
import java.net.URI
import java.net.HttpURLConnection
import java.util.UUID
import java.util.jar.JarInputStream
import java.util.jar.JarEntry
import java.nio.file.Path

object PluginLoader {
    //PRIVATE GLOBALS
    private val plugIDList = mutableListOf<UUID>() //<-- initialize our lists of stuff for loading and closing
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val uRLoaderMap = mutableMapOf<UUID,URLoader>() //<-- we will close these to unload plugins
    private val pluginNameMap = mutableMapOf<UUID,String>() //<-- it felt like I should include this

    //public getter functions
    fun getPlugIDList(): List<UUID> = plugIDList.toList() //<-- return a copy of the List rather than the List itself to prevent concurrent modification exception
    fun getPluginMap(): Map<UUID, MyPlugin> = pluginObjectMap.toMap() //<-- return a copy of the Map rather than the Map itself to prevent concurrent modification exception
    fun getPlugin(plugID: UUID): MyPlugin? = pluginObjectMap[plugID]
    fun getPluginLocation(plugID: UUID): URL? = uRLoaderMap[plugID]?.getURL()//<-- each loader has only 1 loaded plugin class and 1 url anyway
    fun getPluginUUID(plugin: MyPlugin): UUID? = pluginObjectMap.entries.find { it.value == plugin }?.key
    fun getPluginClassName(plugID: UUID): String? = pluginNameMap[plugID]

    //public unload and load functions (Synchronized)
    private val lock = Any() // Shared lock object
    @Synchronized
    fun unloadPlugins(plugIDs: List<UUID>) = plugIDs.forEach { plugID -> unloadPlugin(plugID) }
    @Synchronized
    fun unloadPlugin(plugID: UUID){ //close and remove EVERYWHERE
        try{ uRLoaderMap[plugID]?.close() //<-- if already closed somehow, this can throw
        }catch (e: Exception){e.printStackTrace()}
        uRLoaderMap.remove(plugID) //these don't throw.
        pluginObjectMap.remove(plugID)
        plugIDList.remove(plugID)
        pluginNameMap.remove(plugID)
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        for(entry in uRLoaderMap)try{ entry.value.close() //<-- if already closed somehow, this can throw
            }catch (e: Exception){e.printStackTrace()}
        uRLoaderMap.clear() //these don't throw.
        pluginObjectMap.clear()
        plugIDList.clear()
        pluginNameMap.clear()
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
        try{ // Step 1: getJarURLs(pluginPath: File): List<URL>
            getJarURLs(pluginURI).forEach { plugURL ->

                // Step 2: get ClassLoader for single URL and init list of plugin names
                val loader = URLoader(plugURL)
                val pluginNames = mutableListOf<String>()

                try{ // Step 3: get Class names and if it isSubtypeOf at each url with ClassLoader
                    // this is all defined in our custom class loader at end of file, and contains ASM dependency 
                    pluginNames.addAll(loader.defineAndGetClassInfo(plugURL, MyPlugin::class.java)
                        .filter { (_,v) -> if(v==true)true else false }.map { it.first }) //(GOODBYE REFLECTIONS!!!)

                    // Step 4: loadPluginClasses(URLoader, List<String>, List<String>)
                    //creates plugin instances and populates the globals at the top of file
                    plugIDs.addAll(loadPluginClasses(loader, pluginNames, targetCNames))
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
    private fun loadPluginClasses(loader: URLoader, pluginNames: List<String>, targetCNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        pluginNames.forEach { pluginName -> // use copy of loader for each plugin to allow individual closing
            val plugID = loadPluginClass(loader.copy(), pluginName, targetCNames)
            if(plugID!=null)plugIDs.add(plugID)//<-- if it worked, add uuid to the newly-loaded uuid list
        }
        return plugIDs //<-- return list of new plugins to be launched by callPlugLoader (returns to loadPluginsFromOneURI. End of "Step 4")
    }
    private fun loadPluginClass(loader: URLoader, pluginName: String, targetCNames: List<String> = listOf()): UUID? {
        //first, check our targets list
        if(targetCNames.isEmpty()||(targetCNames.any { target -> ((pluginName == target)) })){
            //then get instance, UUID, then update global list/maps. Return new UUID so user knows which were loaded
            val pluginInstance = loader.loadClass(pluginName).getConstructor().newInstance() as MyPlugin
            val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
            pluginNameMap[pluginUUID] = pluginName //<-- I dont need name map but it felt important or useful
            pluginObjectMap[pluginUUID] = pluginInstance 
            uRLoaderMap[pluginUUID] = loader //<-- we keep track of uRLoaders so we can close them later
            plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
            return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
        } else return null
    }

//-------------------------------------END OF MAIN OBJECT--------PRIVATE CUSTOM CLASS LOADER BELOW------------------------------------------------------------------------

    //The custom class loader that allows for loading from URLs with no class names, and also copying itself (and can only load from 1 url)
    private class URLoader(val plugURL: URL, 
        val parentCL: ClassLoader = PluginLoader::class.java.classLoader, 
        private val urCLCache: MutableMap<String, Class<*>> = HashMap()): 
        URLClassLoader(arrayOf(plugURL), parentCL) {
        //------------------public functions----------------------
        override fun findClass(name: String): Class<*> {
            return urCLCache[name] ?: super.findClass(name)
        }
        //we will create new copy for each plugin so we can close them 1 at a time
        fun copy() = URLoader(plugURL, parentCL, urCLCache.toMutableMap())
        override fun addURL(url: URL){/* NO TOUCHY */}
        fun getURL(): URL = getURLs().get(0)
        //takes url, calls appropriate get bytes function based on protocol. 
        //it then calls define from byte code file which Returns (name, isSubtypeOf)
        var plugInFace: Class<*> = MyPlugin::class.java
        fun defineAndGetClassInfo(plugURL: URL, isSubtypeOf: Class<*>? = plugInFace): List<Pair<String,Boolean>> {
            val bytesOfStuff = mutableListOf<ByteArray?>() //<-- get bytes from stuff
            val nameandimplements = mutableListOf<Pair<String,Boolean>>() //<-- (name, isSubtypeOf)
            //Step 1: Get Bytes
            if(plugURL.protocol == "file")
                bytesOfStuff.add(getBytesFromFile(plugURL))
            if(plugURL.protocol == "http" || plugURL.protocol == "https")
                bytesOfStuff.add(getBytesFromHTTP(plugURL))
            //Step 2: define classes
            bytesOfStuff.forEach { bytecodeFileBytes ->
                if(bytecodeFileBytes!=null)nameandimplements.addAll(
                    defineClassFromByteCodeFile(bytecodeFileBytes, plugURL, isSubtypeOf))
            }
            return nameandimplements
        }

        //------------------Private util functions-------------------------------
        //these 2 are utils for defineAndGetClassInfo. 
        //If you can get bytes of it, you can load it.
        //(assuming it has a url ending in .jar or .class, which getJarURLs already took care of)
        //to add new protocols:
        //add a getBytes here, call in defineAndGetClassInfo above,
        //and then show getJarURLs how to find the URL for it
        private fun getBytesFromFile(plugURL: URL): ByteArray {
            val fileinputstream = FileInputStream(File(plugURL.toURI()))
            val fileBytes = fileinputstream.readAllBytes()
            fileinputstream.close()
            return fileBytes
        }
        private fun getBytesFromHTTP(plugURL: URL): ByteArray? {
            var urlBytes: ByteArray? = null
            try {
                val urlConnection = plugURL.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = urlConnection.inputStream
                    urlBytes = inputStream.readBytes()
                    inputStream.close()
                    urlConnection.disconnect()
                }
            } catch (e: Exception) { e.printStackTrace() }
            return urlBytes
        }

        //Private functions that actually load the stuff--------------------------------

        //call function for jar if jar or class if class
        private fun defineClassFromByteCodeFile(urlBytes: ByteArray, plugURL: URL, isSubtypeOf: Class<*>? = plugInFace): List<Pair<String,Boolean>> {
            val classList= mutableListOf<Pair<String,Boolean>>()
            try{
                if(plugURL.toString().endsWith(".jar"))
                    classList.addAll(defineClassesFromJarBytes(urlBytes, isSubtypeOf))
                if(plugURL.toString().endsWith(".class")){
                    val classInfo = defineClassFromBytes(urlBytes, isSubtypeOf)
                    val className = classInfo.first
                    if(className!=null)classList.add(Pair(className, classInfo.second))
                }
            } catch (e: Exception) { e.printStackTrace() }
            return classList
        }

        //This just calls defineClassFromBytes on jar entries
        private fun defineClassesFromJarBytes(jarBytes: ByteArray, isSubtypeOf: Class<*>? = plugInFace): List<Pair<String,Boolean>> {
            val jarClassList = mutableListOf<Pair<String,Boolean>>()
            JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
                var entry = jis.getNextJarEntry()
                while (entry!=null) {
                    if (!entry.isDirectory && entry.name.endsWith(".class")) {
                        try{
                            val classInfo = defineClassFromBytes(jis.readAllBytes(), isSubtypeOf)
                            val className = classInfo.first
                            if(className!=null)jarClassList.add(Pair(className, classInfo.second))
                        } catch (e: Exception){ e.printStackTrace() }
                    }
                    jis.closeEntry()
                    entry = jis.getNextJarEntry()
                }
            }
            return jarClassList
        }

        //take byte arrays, gets info, adds class to loader to be run with name later. Uses "org.ow2.asm:asm:9.5".
        //Since I needed this to get name, may as well use to get subtype, and not need reflections
        private fun defineClassFromBytes(classBytes: ByteArray, isSubtypeOf: Class<*>? = plugInFace): Pair<String?,Boolean> {
            var classInfo: Pair<String?,Boolean> = Pair(null, false)
            // BEHOLD!! "org.ow2.asm:asm:9.5" !!!!!!!!!!!!!!!!!
            val classReader = ClassReader(classBytes)
            classReader.accept(object : ClassVisitor(Opcodes.ASM9) {
                override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<String>?) {
                    if(name!=null){
                        val launchName = name.replace('/', '.')
                        urCLCache[launchName] = defineClass(launchName, classBytes, 0, classBytes.size) //<-- defines classes before filter, because otherwise they will be unreachable
                        if(isSubtypeOf!=null){
                            val isExtensionOfPlugin: Boolean
                            if(superName==null) isExtensionOfPlugin = false
                            else isExtensionOfPlugin = (Type.getInternalName(isSubtypeOf) == superName)
                            val isImplementationOfPlugin = (interfaces?.contains(Type.getInternalName(isSubtypeOf)) ?: false)
                            classInfo = Pair(launchName,(isImplementationOfPlugin||isExtensionOfPlugin))
                        } else classInfo = Pair(launchName,true)
                    }
                }
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            //Yaaaaaay! Reflections can reflect on this! A class? from bytes? EVERYTHING IS BYTES
            return classInfo
        }
    }
}