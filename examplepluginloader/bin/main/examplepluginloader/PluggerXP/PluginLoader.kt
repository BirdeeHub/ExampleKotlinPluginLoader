package examplepluginloader.PluggerXP

import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import org.objectweb.asm.Type
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.InputStream
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.net.URL
import java.net.URLClassLoader
import java.net.URI
import java.util.UUID
import java.util.jar.JarInputStream
import java.util.jar.JarEntry
import java.nio.file.Path

object PluginLoader {
    private val plugIDList = mutableListOf<UUID>() //<-- initialize our lists of stuff for loading and closing
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val uRLoaderMap = mutableMapOf<UUID,URLoader>() //<-- we will close these to unload plugins

    //public getter functions
    fun getPlugIDList(): List<UUID> = plugIDList.toList() //<-- return a copy of the List rather than the List itself to prevent concurrent modification exception
    fun getPluginMap(): Map<UUID, MyPlugin> = pluginObjectMap.toMap() //<-- return a copy of the Map rather than the Map itself to prevent concurrent modification exception
    fun getPlugin(plugID: UUID): MyPlugin? = pluginObjectMap[plugID]
    fun getPluginLocation(plugID: UUID): URL? = uRLoaderMap[plugID]?.getURL()//<-- each loader has only 1 class and 1 url anyway
    fun getPluginUUID(plugin: MyPlugin): UUID? = pluginObjectMap.entries.find { it.value == plugin }?.key

    //unload and load functions (Synchronized)
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
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        for(entry in uRLoaderMap)try{ entry.value.close() //<-- if already closed somehow, this can throw
            }catch (e: Exception){e.printStackTrace()}
        uRLoaderMap.clear() //these don't throw.
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
    fun loadPluginsFromURIs(api: MyAPI, pluginURIs: List<URI>, targetPluginFullClassNames: List<String> = listOf()): List<UUID> = 
        callPlugLoader(api, pluginURIs, targetPluginFullClassNames)

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

                // Step 2: get ClassLoader for single URL and init list of plugin Class objects
                val loader = URLoader(plugURL)
                val pluginNames = mutableListOf<String>()

                try{ // Step 3: get Class objects at each url with ClassLoader
                    if(plugURL.protocol == "file")
                        pluginNames.addAll(getPluginsFromFile(plugURL, loader))
                    if(plugURL.protocol == "http" || plugURL.protocol == "https")
                        pluginNames.addAll(getPluginsFromHTTP(plugURL, loader))

                    // Step 4: loadPluginClasses(List<Class<out MyPlugin>>, URLoader, List<String>)
                    plugIDs.addAll(loadPluginClasses(pluginNames, loader, targetCNames))
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
            // if file:
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
            // else if web http/s:
            } else if(pluginPath.protocol == "http" || pluginPath.protocol == "https" && 
                (pluginPath.toString().endsWith(".jar")||pluginPath.toString().endsWith(".class"))) {
                return listOf(pluginPath)
            } else return listOf()
        }catch(e: Exception){ e.printStackTrace(); return listOf() }
    }

    // Get all subtypes of MyPlugin using Reflections from File
    private fun getPluginsFromFile(plugURL: URL, loader: URLoader): List<String> = Reflections( // "org.reflections:reflections:0.10.2"
        ConfigurationBuilder().addUrls(plugURL).addClassLoaders(loader)).getSubTypesOf(MyPlugin::class.java).map { it.name }
    // Get all subtypes of MyPlugin from Web (using reflections when i was using asm to get name was redundant)
    private fun getPluginsFromHTTP(plugURL: URL, loader: URLoader): List<String> = 
        loader.defineClassesFromHTTPurl(plugURL).filter { (_,v) -> if(v==true)true else false }.map { it.first }

    //Once you finally have the Class objects
    private fun loadPluginClasses(pluginNames: List<String>, loader: URLoader, targetCNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        pluginNames.forEach { pluginName -> // use copy of loader to allow individual closing
            val plugID = loadPluginClass(loader.copy(), pluginName, targetCNames)
            if(plugID!=null)plugIDs.add(plugID)//<-- if it worked, add uuid to the newly-loaded uuid list
        }
        return plugIDs
    }
    private fun loadPluginClass(loader: URLoader, pluginName: String, targetCNames: List<String> = listOf()): UUID? {
        if(targetCNames.isEmpty()||(targetCNames.any { target -> ((pluginName == target)) })){
            val pluginInstance = loader.loadClass(pluginName).getConstructor().newInstance() as MyPlugin
            val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
            pluginObjectMap[pluginUUID] = pluginInstance //add stuff into respective maps using UUID as the key
            uRLoaderMap[pluginUUID] = loader //<-- we keep track of uRLoaders so we can close them later
            plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
            return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
        } else return null
    }

//-------------------------------------END OF MAIN OBJECT--------PRIVATE CUSTOM CLASS LOADER BELOW------------------------------------------------------------------------
    //The custom class loader that allows for loading from bytes with no class names, and also copying itself (and can only load from 1 url)
    private class URLoader(val plugURL: URL): URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader){
        var pluginFace: Class<*> = MyPlugin::class.java
        override fun addURL(url: URL){}
        fun getURL(): URL = getURLs().get(0)
        fun copy() = this
        fun defineClassesFromHTTPurl(plugURL: URL, implements: Class<*>? = pluginFace): List<Pair<String,Boolean>> {
            val classList= mutableListOf<Pair<String,Boolean>>()
            try{
                val httpClient = HttpClients.createDefault()
                val httpGet = HttpGet(plugURL.toURI())
                val response = httpClient.execute(httpGet)
                val inputStream = response.entity.content
                val urlBytes = readBytesToArray(inputStream)
                inputStream.close()
                response.close()
                httpClient.close()
                if(plugURL.toString().endsWith(".jar"))classList.addAll(defineClassesFromJarBytes(urlBytes, implements))
                if(plugURL.toString().endsWith(".class")){
                    val classInfo = defineClassFromBytes(urlBytes, implements)
                    val className = classInfo.first
                    if(className!=null)classList.add(Pair(className, classInfo.second))
                }
            } catch (e: Exception) { e.printStackTrace() }
            return classList
        }
        private fun defineClassesFromJarBytes(jarBytes: ByteArray, implements: Class<*>? = pluginFace): List<Pair<String,Boolean>> {
            val jarClassList = mutableListOf<Pair<String,Boolean>>()
            JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
                var entry = jis.getNextJarEntry()
                while (entry!=null) {
                    if (!entry.isDirectory && entry.name.endsWith(".class")) {
                        try{
                            val classInfo = defineClassFromBytes(readBytesToArray(jis), implements)
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
        private fun readBytesToArray(inputStream: InputStream): ByteArray {//<-- this is needed in jar class loading and also for getting web data
            val buffer = ByteArray(1024)
            val output = ByteArrayOutputStream()
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            return output.toByteArray()
        }
        private fun defineClassFromBytes(classBytes: ByteArray, implements: Class<*>? = pluginFace): Pair<String?,Boolean> {//<-- uses "org.ow2.asm:asm:9.5"
            var classInfo: Pair<String?,Boolean> = Pair(null, false) //since i needed this to get name, may as well use to get subtype,
            val classReader = ClassReader(classBytes)                //otherwise reflections makes a second get request
            classReader.accept(object : ClassVisitor(Opcodes.ASM9) {
                override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<String>?) {
                    if(name!=null){
                        val launchName = name.replace('/', '.')
                        defineClass(launchName, classBytes, 0, classBytes.size)
                        var isSubtypeOfPlugin = interfaces?.contains(Type.getInternalName(implements)) ?: false
                        classInfo = Pair(launchName,isSubtypeOfPlugin)
                    }
                }
            }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
            return classInfo
        }
    }
}