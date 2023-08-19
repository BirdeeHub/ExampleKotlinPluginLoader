package examplepluginloader.PluggerXP

import java.util.jar.JarInputStream
import java.util.UUID
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.File
import java.net.URL
import java.net.HttpURLConnection
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import kotlin.collections.forEach

interface URLclassInfo {
    val urURL: URL
    val version: Int
    val access: Int
    val name: String?
    val signature: String?
    val xtnds: String?
    val imps: List<String>?
    fun isImpOf(internalName: String): Boolean
    var optUUID: UUID?
}
class JByteCodeURLINFO(public val yourURL: URL){
    val isSupported: Boolean
    val classInfoAtURL: List<URLclassInfo>?
    val rescInJar: List<URL>?
    companion object {
        fun getExtClassName(internalName: String): String = 
            Type.getObjectType(internalName).getClassName()
        fun getInternalCName(obj: Class<*>): String = 
            Type.getInternalName(obj)
    }
    fun getClassInfoByExtName(name: String): URLclassInfo? {
        val tempList = classInfoAtURL?.filter { it.name == name.replace('.','/') }
        if(tempList==null || tempList.isEmpty() ) return null
        else if(tempList.size>1) throw Exception("Multiple classes found at URL with same fully qualified name")
        else return tempList[0]
    }
    init{
        val bytesOfStuff = mutableListOf<ByteArray?>()
        if(yourURL.protocol == "file")
            bytesOfStuff.add(File(yourURL.toURI()).inputStream().readAllBytes())
        if(yourURL.protocol == "http" || yourURL.protocol == "https")
            bytesOfStuff.add(getBytesFromHTTP(yourURL))
        //add other protocols here if desired
        if(bytesOfStuff.isEmpty()){ 
            isSupported = false
            classInfoAtURL=null
            rescInJar=null
        } else {
            isSupported = true
            val uRLCIs = mutableListOf<URLclassInfo>()
            val uRLRes = mutableListOf<URL>()
            bytesOfStuff.forEach { urlBytes -> 
                var cinfo: List<URLclassInfo>? = null
                var rinfo: List<URL>? = null
                if(urlBytes!=null){
                    val urlInfo = CIfromBCode(yourURL,urlBytes)
                    cinfo = urlInfo.first
                    rinfo = urlInfo.second
                }
                if(cinfo!=null){
                    uRLCIs.addAll(cinfo)
                }
                if(rinfo!=null){
                    uRLRes.addAll(rinfo.toList())
                }
            }
            if(uRLCIs.isEmpty())classInfoAtURL=null
            else classInfoAtURL=uRLCIs.toList()
            if(uRLRes.isEmpty())rescInJar=null
            else rescInJar=uRLRes.toList()
        }
    }
    private fun getBytesFromHTTP(yourURL: URL): ByteArray? =
        try {
            val urlBytes: ByteArray?
            val urlConnection = yourURL.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = urlConnection.inputStream
                urlBytes = inputStream.readBytes()
                inputStream.close()
                urlConnection.disconnect()
            } else urlBytes = null
            urlBytes
        } catch (e: Exception) { e.printStackTrace(); null }

    //call function for jar if jar or class if class
    private fun CIfromBCode(yourURL: URL, urlBytes: ByteArray): Pair<List<URLclassInfo>?, List<URL>?> =
        try{
            if(yourURL.toString().endsWith(".jar"))
                CIfromfromJar(urlBytes)
            else if(yourURL.toString().endsWith(".class")){
                Pair(listOf(getCINFO(ByteArrayInputStream(urlBytes))), null)
            } else Pair(null, null)
        } catch (e: Exception) { e.printStackTrace(); Pair(null, null) }
    
    //This just calls defineClassFromBytes on jar entries
    private fun CIfromfromJar(jarBytes: ByteArray): Pair<List<URLclassInfo>?, List<URL>?> {
        val jarClassList = mutableListOf<URLclassInfo>()
        val jarresourcelist = mutableListOf<URL>()
        JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
            var entry = jis.getNextJarEntry()
            while (entry!=null) {
                if (!entry.isDirectory && entry.name.endsWith(".class")) {
                    try{ jarClassList.add(getCINFO(jis))
                    } catch (e: Exception){ e.printStackTrace() }
                } else if(!entry.isDirectory)jarresourcelist.add(URL("jar:$yourURL!/$entry.name"))
                jis.closeEntry()
                entry = jis.getNextJarEntry()
            }
        }
        if(jarClassList.isEmpty() && jarresourcelist.isEmpty()) 
            return Pair(null,null)
        else if(jarClassList.isEmpty() && !jarresourcelist.isEmpty()) 
            return Pair(null,jarresourcelist.toList())
        else if(!jarClassList.isEmpty() && jarresourcelist.isEmpty()) 
            return Pair(jarClassList.toList(),null)
        else return Pair(jarClassList.toList(),jarresourcelist.toList())
    }

    //take byte arrays of .class files, Uses "org.ow2.asm:asm:9.5" to get info
    private fun getCINFO(classStream: InputStream): URLclassInfo {
        var classInfo: URLclassInfo = object: URLclassInfo {
            override val urURL: URL = yourURL
            override val version: Int = -1
            override val access: Int = -1
            override val name: String? = null
            override val signature: String? = null
            override val xtnds: String? = null
            override val imps: List<String>? = null
            override fun isImpOf(internalName: String): Boolean = false
            override var optUUID: UUID? = null
        }
        val classReader = ClassReader(classStream)
        classReader.accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<String>?) {
                classInfo = object: URLclassInfo {
                    override val urURL: URL = yourURL
                    override val version: Int = version
                    override val access: Int = access
                    override val name: String? = name
                    override val signature: String? = signature
                    override val xtnds: String? = superName
                    override val imps: List<String>? = interfaces?.toList()
                    override fun isImpOf(internalName: String): Boolean = interfaces?.contains(internalName) ?: false
                    override var optUUID: UUID? = null
                };
            }
        }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        return classInfo
    }
}