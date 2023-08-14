package examplepluginloader.PluggerXP

import examplepluginloader.api.MyPlugin //<-- this is MyPlugin interface. To make a plugin, implement the interface and its functions
import examplepluginloader.api.MyAPI //<-- this gets passed to the plugin via the myPluginInstance.launchPlugin(api: MyAPI) function that you must implement
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.InputStream
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
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
    private val pluginClassMap = mutableMapOf<UUID,KClass<out MyPlugin>>() //<-- initialize our lists of stuff for loading and closing
    private val pluginObjectMap = mutableMapOf<UUID,MyPlugin>() //<-- this one has the loaded instances
    private val uRLoaderMap = mutableMapOf<UUID,URLoader>() //<-- we will close these to unload plugins
    private val plugIDList = mutableListOf<UUID>()

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
        pluginClassMap.remove(plugID)
        pluginObjectMap.remove(plugID)
        plugIDList.remove(plugID)
    }
    @Synchronized
    fun unloadAllPlugins() { //close and clear ALL everywhere
        for(entry in uRLoaderMap)try{ entry.value.close() //<-- if already closed somehow, this can throw
            }catch (e: Exception){e.printStackTrace()}
        uRLoaderMap.clear() //these don't throw.
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
    private fun loadPluginsFromOneURI(pluginURI: URI, targetClassNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        try{ // Step 1: getJarURLs(pluginPath: File): List<URL>
            getJarURLs(pluginURI).forEach { plugURL ->

                // Step 2: get ClassLoader for single URL and init list of plugin Class objects
                val loader = URLoader(plugURL)
                val pluginClasses = mutableListOf<Class<out MyPlugin>>()

                try{ // Step 3: get Class objects at each url with ClassLoader
                    if(plugURL.protocol == "file")
                        pluginClasses.addAll(getPluginsFromFile(plugURL, loader))
                    if(plugURL.protocol == "http" || plugURL.protocol == "https")
                        pluginClasses.addAll(getPluginsFromHTTP(plugURL, loader))

                    // Step 4: loadPluginClasses(List<Class<out MyPlugin>>, URLoader, List<String>)
                    plugIDs.addAll(loadPluginClasses(pluginClasses, loader, targetClassNames))
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
    private fun getPluginsFromFile(plugURL: URL, loader: URLoader): List<Class<out MyPlugin>> = Reflections(
        ConfigurationBuilder().addUrls(plugURL).addClassLoaders(loader)).getSubTypesOf(MyPlugin::class.java).toList()
    // Get all subtypes of MyPlugin using Reflections from Web
    private fun getPluginsFromHTTP(plugURL: URL, loader: URLoader): List<Class<out MyPlugin>> { 
        val pluginClasses = mutableListOf<Class<out MyPlugin>>()
        try {
            val urlBytes = URLoader.getBytesFromURL(plugURL)
            if(plugURL.toString().endsWith(".jar")){
                val config = ConfigurationBuilder.build(loader.defineClassesFromJarBytes(urlBytes)).addUrls(plugURL).addClassLoaders(loader)
                pluginClasses.addAll(Reflections(config).getSubTypesOf(MyPlugin::class.java))
            } else if(plugURL.toString().endsWith(".class")){
                var uRLClassName = plugURL.toURI().path.substringAfterLast('/').removeSuffix(".class")
                if(uRLClassName.isNotEmpty()){
                    val config = ConfigurationBuilder.build(loader.defineClassFromClassBytes(urlBytes)).addUrls(plugURL).addClassLoaders(loader)
                    pluginClasses.addAll(Reflections(config).getSubTypesOf(MyPlugin::class.java))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return pluginClasses
    }

    //Once you finally have the Class objects
    private fun loadPluginClasses(pluginClasses: List<Class<out MyPlugin>>, loader: URLoader, targetClassNames: List<String> = listOf()): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        pluginClasses.forEach { pluginClass -> // use copy of loader to allow individual closing, also map to KClass
            val plugID = loadPluginClass(loader.copy(), pluginClass.kotlin, targetClassNames)
            if(plugID!=null)plugIDs.add(plugID)//<-- if it worked, add uuid to the newly-loaded uuid list
        }
        return plugIDs
    }
    private fun loadPluginClass(loader: URLoader, pluginClass: KClass<out MyPlugin>, targetClassNames: List<String> = listOf()): UUID? {
        if(targetClassNames.isEmpty() || (targetClassNames.any { target -> ((pluginClass.qualifiedName == target)||(pluginClass.simpleName == target)) })){
            var launchableName = pluginClass.qualifiedName //<-- get class name
            if(launchableName==null)launchableName=pluginClass.simpleName //<-- if not in package it may only have simpleName
            if(launchableName!=null){ // if it has a name at all, launch it and update lists
                val pluginInstance = loader.loadClass(launchableName).getConstructor().newInstance() as MyPlugin
                val pluginUUID = UUID.randomUUID() //<-- Use a UUID to keep track of them.
                pluginClassMap[pluginUUID] = pluginClass //add stuff into respective maps using UUID as the key
                pluginObjectMap[pluginUUID] = pluginInstance
                uRLoaderMap[pluginUUID] = loader //<-- we keep track of uRLoaders so we can close them later
                plugIDList.add(pluginUUID) //<-- add new uuid to the actual UUID list
                return pluginUUID //<-- return uuid to add to the newly-loaded uuid list
            } else return null
        } else return null
    }

//-------------------------------------END OF MAIN OBJECT--------PRIVATE CUSTOM CLASS LOADER BELOW------------------------------------------------------------------------
    //The custom class loader that allows for loading from bytes with no class names, and also copying itself (and can only load from 1 url)
    private class URLoader(val plugURL: URL): URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader){
        fun copy() = this
        fun getURL(): URL = getURLs().get(0)
        override fun addURL(url: URL){}
        fun defineClassFromClassBytes(classBytes: ByteArray): Class<*> {
            return defineClass(getClassNameFromBytes(classBytes), classBytes, 0, classBytes.size)
        }
        fun defineClassesFromJarBytes(jarBytes: ByteArray): List<Class<*>> {
            val jarClassList = mutableListOf<Class<*>>()
            JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
                var entry = jis.getNextJarEntry()
                while (entry!=null) {
                    if (!entry.isDirectory && entry.name.endsWith(".class")) {
                        val classBytes = readBytesToArray(jis)
                        try{
                            val uRLClassName = getClassNameFromBytes(classBytes)
                            if(uRLClassName!=null){
                                val uRLClass = defineClass(uRLClassName, classBytes, 0, classBytes.size)
                                jarClassList.add(uRLClass)
                            }
                        } catch (e: Exception){ e.printStackTrace() }
                    }
                    jis.closeEntry()
                    entry = jis.getNextJarEntry()
                }
            }
            return jarClassList
        }
        companion object {
            fun getBytesFromURL(plugURL: URL): ByteArray { //"org.apache.httpcomponents:httpclient:4.5.9"
                val httpClient = HttpClients.createDefault()
                val httpGet = HttpGet(plugURL.toURI())
                val response = httpClient.execute(httpGet)
                val inputStream: InputStream = response.entity.content
                val urlBytes = readBytesToArray(inputStream)
                response.close()
                httpClient.close()
                inputStream.close()
                return urlBytes
            }
            private fun getClassNameFromBytes(classBytes: ByteArray): String? {//<-- uses "org.ow2.asm:asm:9.5" to get the class name properly
                var className: String? = null
                val classReader = ClassReader(classBytes)
                classReader.accept(object : ClassVisitor(Opcodes.ASM9) {
                    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<String>?) {
                        className = name?.replace('/', '.')
                    }
                }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                return className
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
        }
    }
}