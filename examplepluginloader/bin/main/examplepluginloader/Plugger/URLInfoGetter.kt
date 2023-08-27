package examplepluginloader.Plugger

import java.net.URL
import java.net.HttpURLConnection
import java.io.File
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.jar.JarInputStream
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
object URLInfoGetter {
    fun getInfo(yourURL: URL): Triple<Boolean, List<JByteCodeURLINFO.URLclassInfo>?, List<URL>?>{
        val isSupported: Boolean
        val classInfoAtURL: List<JByteCodeURLINFO.URLclassInfo>?
        val rescInJar: List<URL>?
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
            val uRLCIs = mutableListOf<JByteCodeURLINFO.URLclassInfo>()
            val uRLRes = mutableListOf<URL>()
            bytesOfStuff.forEach { urlBytes -> 
                var cinfo: List<JByteCodeURLINFO.URLclassInfo>? = null
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
        return Triple(isSupported, classInfoAtURL, rescInJar)
    }
    private fun getBytesFromHTTP(yourURL: URL): ByteArray? = try {
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
    private fun CIfromBCode(yourURL: URL, urlBytes: ByteArray): Pair<List<JByteCodeURLINFO.URLclassInfo>?, List<URL>?> = try{
        if(yourURL.toString().endsWith(".jar"))
            CIfromfromJar(yourURL, urlBytes)
        else if(yourURL.toString().endsWith(".class")){
            Pair(listOf(getCINFO(yourURL, ByteArrayInputStream(urlBytes))), null)
        } else Pair(null, null)
    } catch (e: Exception) { e.printStackTrace(); Pair(null, null) }
    
    //This just calls defineClassFromBytes on jar entries
    private fun CIfromfromJar(yourURL: URL, jarBytes: ByteArray): Pair<List<JByteCodeURLINFO.URLclassInfo>?, List<URL>?> {
        val jarClassList = mutableListOf<JByteCodeURLINFO.URLclassInfo>()
        val jarresourcelist = mutableListOf<URL>()
        JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
            var entry = jis.getNextJarEntry()
            while (entry!=null) {
                if (!entry.isDirectory && entry.name.endsWith(".class")) {
                    try{ jarClassList.add(getCINFO(yourURL, jis))
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
    private fun getCINFO(yourURL: URL, classStream: InputStream): JByteCodeURLINFO.URLclassInfo {
        var classInfo: JByteCodeURLINFO.URLclassInfo = JByteCodeURLINFO.URLclassInfo(yourURL, -1, -1, null, null, null, null, null)
        val classReader = ClassReader(classStream)
        classReader.accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<String>?) {
                classInfo = JByteCodeURLINFO.URLclassInfo(yourURL, version, access, name, signature, superName, interfaces?.toList(), null)
            }
        }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        return classInfo
    }
}