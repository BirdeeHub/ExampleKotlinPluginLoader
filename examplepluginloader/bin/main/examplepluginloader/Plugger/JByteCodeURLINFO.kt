package examplepluginloader.Plugger

import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.jar.JarInputStream
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

// This gets class info from a url and then after that just contains a list of the following
// interface and 3 functions
class JByteCodeURLINFO(public val yourURL: URL) {
    val protocolSupported: Boolean
    val classInfoAtURL: List<URLclassInfo>
    val rescInJar: Map<String, ByteArray>
    val urlBytes: ByteArray?
    val classBytes: Map<String, ByteArray>
    class URLclassInfo(
            override val urURL: URL,
            override val entryName: String,
            val version: Int,
            val access: Int,
            val name: String?,
            val signature: String?,
            val xtnds: String?,
            val imps: List<String>?,
            var optUUID: UUID?
    ) : DangerZone.CInfo() {
        fun isImpOf(obj: Class<*>): Boolean = imps?.contains(Type.getInternalName(obj)) ?: false
    }
    companion object {
        fun getExternalName(internalName: String): String =
                Type.getObjectType(internalName).getClassName()
        fun getExternalName(obj: Class<*>): String = Type.getType(obj).getClassName()
    }
    init {
        urlBytes = getBytesFromURL(yourURL)
        if (urlBytes != null) {
            if (yourURL.toString().endsWith(".jar")) {
                protocolSupported = true
                val temp = bytesFromfromJar(urlBytes)
                classBytes = temp.first ?: mapOf()
                rescInJar = temp.second ?: mapOf()
                val urlCInfos = mutableListOf<URLclassInfo>()
                classBytes.forEach { urlCInfos.addAll(getCINFO(yourURL, it.key, it.value)) }
                classInfoAtURL = urlCInfos
            } else if (yourURL.toString().endsWith(".class")) {
                protocolSupported = true
                classBytes = mapOf(Pair(yourURL.file, urlBytes))
                rescInJar = mapOf()
                classInfoAtURL = getCINFO(yourURL, yourURL.file, urlBytes)
            } else {
                protocolSupported = false
                classBytes = mapOf()
                rescInJar = mapOf()
                classInfoAtURL = listOf()
            }
        } else {
            protocolSupported = false
            classBytes = mapOf()
            rescInJar = mapOf()
            classInfoAtURL = listOf()
        }
    }
    private fun getBytesFromURL(yourURL: URL): ByteArray? =
            if (yourURL.protocol == "file") {
                File(yourURL.toURI()).inputStream().readAllBytes()
            } else if (yourURL.protocol == "http" || yourURL.protocol == "https") {
                getBytesFromHTTP(yourURL)
            } else null
    // add other protocols here if desired
    private fun getBytesFromHTTP(yourURL: URL): ByteArray? =
            try {
                val urlConnection = yourURL.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = urlConnection.inputStream
                    val urlBytes = inputStream.readBytes()
                    inputStream.close()
                    urlConnection.disconnect()
                    urlBytes
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    // This just calls defineClassFromBytes on jar entries
    private fun bytesFromfromJar(
            jarBytes: ByteArray
    ): Pair<Map<String, ByteArray>?, Map<String, ByteArray>?> {
        val jarClassMap = mutableMapOf<String, ByteArray>()
        val jarresourceMap = mutableMapOf<String, ByteArray>()
        JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
            var entry = jis.getNextJarEntry()
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".class")) {
                    try {
                        jarClassMap[entry.name] = jis.readAllBytes()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (!entry.isDirectory) jarresourceMap[entry.name] = jis.readAllBytes()
                jis.closeEntry()
                entry = jis.getNextJarEntry()
            }
        }
        if (jarClassMap.isEmpty() && jarresourceMap.isEmpty()) return Pair(null, null)
        else if (jarClassMap.isEmpty() && !jarresourceMap.isEmpty())
                return Pair(null, jarresourceMap)
        else if (!jarClassMap.isEmpty() && jarresourceMap.isEmpty()) return Pair(jarClassMap, null)
        else return Pair(jarClassMap, jarresourceMap)
    }

    // take byte arrays of .class files, Uses "org.ow2.asm:asm:9.5" to get info
    private fun getCINFO(
            yourURL: URL,
            entryName: String,
            classBytes: ByteArray
    ): List<URLclassInfo> {
        val classInfo = mutableListOf<URLclassInfo>()
        val classReader = ClassReader(classBytes)
        classReader.accept(
                object : ClassVisitor(Opcodes.ASM9) {
                    override fun visit(
                            version: Int,
                            access: Int,
                            name: String?,
                            signature: String?,
                            superName: String?,
                            interfaces: Array<String>?
                    ) {
                        classInfo.add(
                                URLclassInfo(
                                        yourURL,
                                        entryName,
                                        version,
                                        access,
                                        name,
                                        signature,
                                        superName,
                                        interfaces?.toList(),
                                        null
                                )
                        )
                    }
                },
                ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
        )
        return classInfo
    }
}

