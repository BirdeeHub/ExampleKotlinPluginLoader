package examplepluginloader.Plugger

import java.net.URI
import java.net.URL
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.ClassVisitor
class DangerZone(pluginLocation: URI) {
    val jByteCodeInfos: List<JByteCodeURLINFO>
    abstract class CInfo {
        var isBlocked = false
        abstract val urURL: URL
        abstract val entryName: String
        fun sameItemAs(other: Any?): Boolean =
            if(this === other) true
            else if(other !is CInfo) false
            else (other.urURL == this.urURL && other.entryName == this.entryName)
        override fun equals(other: Any?): Boolean =
            if(other==null) false
            else if(other::class.java != this::class.java) false
            else sameItemAs(other)
    }
    class ClassDangerCheckInfo(
        override val urURL: URL,
        override val entryName: String,
        //TODO: Add all of the fields from ClassVisitors useful for finding danger
    ): CInfo() {}
    class RescDangerCheckInfo(
        override val urURL: URL,
        override val entryName: String,
        //TODO: add output of Resource Scan
    ): CInfo() {}
    init{
        val infos = mutableListOf<JByteCodeURLINFO>()
        PluginManager.getJarURLs(pluginLocation).forEach { plugURL ->
            infos.add(JByteCodeURLINFO(plugURL))
        }
        jByteCodeInfos = infos
    }
    fun asmDangerScan(bcINFO: JByteCodeURLINFO): List<CInfo>{
        val totalCInfos = mutableListOf<CInfo>()
        bcINFO.classBytes.forEach { (k,v) -> totalCInfos.addAll(getClassDangerInfo(bcINFO.yourURL, k, v)) }
        return totalCInfos
    }
    private fun getClassDangerInfo(yourURL: URL, entryName: String, classBytes: ByteArray): List<ClassDangerCheckInfo> {
        val classInfo = mutableListOf<ClassDangerCheckInfo>()
        val classReader = ClassReader(classBytes)
        classReader.accept(object : ClassVisitor(Opcodes.ASM9) {
            //TODO: visit all the other things and add the object(s) to the list so we can dangerCheck() later!
            //Add them all to 1 object for ease of use later
        }, ClassReader.EXPAND_FRAMES)
        return classInfo
    }
    fun rescDangerScan(bcINFO: JByteCodeURLINFO): List<CInfo> {
        val totalCInfos = mutableListOf<CInfo>()
        bcINFO.rescInJar.forEach { (k,v) -> totalCInfos.addAll(getClassDangerInfo(bcINFO.yourURL, k, v)) }
        return totalCInfos
    }
    private fun getRescDangerInfo(yourURL: URL, entryName: String, RescBytes: ByteArray): List<RescDangerCheckInfo> {
        val rescInfo = mutableListOf<RescDangerCheckInfo>()
        //TODO scan the resource
        return rescInfo
    }

    //Usage: run on each supplied URL before passing to loader. 
    //Do checks, pass updated URLclassInfos with blocking info to PluginManager, 
    //so it knows which have been blocked and also does not need to search for names again

    //TODO1: Use info from JByteCodeURLINFO to do Danger Check
    //TODO1.1: Compose jByteCodeInfos checks
    //TODO1.2: Single jByteCodeInfo check
    //TODO1.3: check classes
    //TODO1.4: check resources
    //TODO1.5: set isBlocked variables on all URLclassInfos in each ByteCode file that should be blocked.
                //This prevents 1 plugin in the jar passing the test while the other fails, 
                //which would allow the second one to bypass the block

    //TODO2: functions to generate report info
    //TODO3: functions for user approval of block or override

    //TODO4: set blocked plugins as blocked in their URLclassInfos and pass to PluginManager
            //this will stop the plugin from being loaded, 
            //and it will also eliminate the need to do another download to check for names
}