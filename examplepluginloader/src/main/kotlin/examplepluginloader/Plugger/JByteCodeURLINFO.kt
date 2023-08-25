package examplepluginloader.Plugger

import org.objectweb.asm.Type
import java.net.URL
import java.util.UUID
//This gets class info from a url and then after that just contains a list of the following interface and 3 functions
class JByteCodeURLINFO(public val yourURL: URL){
    val isSupported: Boolean
    val classInfoAtURL: List<URLclassInfo>?
    val rescInJar: List<URL>?
    init{ 
        val info = URLInfoGetter.getInfo(yourURL) 
        isSupported = info.first
        classInfoAtURL = info.second
        rescInJar = info.third
    }
    //utility
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
}
class URLclassInfo(
    val urURL: URL,
    val version: Int,
    val access: Int,
    val name: String?,
    val signature: String?,
    val xtnds: String?,
    val imps: List<String>?,
    var optUUID: UUID?
){
    fun isImpOf(internalName: String): Boolean = imps?.contains(internalName) ?: false
}