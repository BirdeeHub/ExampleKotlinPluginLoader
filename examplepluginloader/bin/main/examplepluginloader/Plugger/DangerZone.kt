package examplepluginloader.Plugger

import java.net.URI
class DangerZone(pluginLocation: URI) {
    val jByteCodeInfos: List<JByteCodeURLINFO>
    init{
        val infos = mutableListOf<JByteCodeURLINFO>()
        PluginManager.getJarURLs(pluginLocation).forEach { plugURL ->
            infos.add(JByteCodeURLINFO(plugURL))
        }
        jByteCodeInfos = infos
    }
    //Usage: run on each supplied URL before passing to loader. 
    //Do checks, pass updated URLclassInfos with blocking info to PluginManager, 
    //so it knows which have been blocked and also does not need to search for names again

    //TODO1: Use Info from jByteCodeInfo.dangerScan() to do Danger Check
    //TODO1.1: Composed jByteCodeInfos check
    //TODO1.2: Single jByteCodeInfo check
    //TODO1.3: check classes
    //TODO1.4: check resources
    //TODO1.4: set isBlocked variables on all URLclassInfos in each ByteCode file that should be blocked.
                //This prevents 1 plugin in the jar passing the test while the other fails, 
                //which would allow the second one to bypass the block

    //TODO2: functions to generate report info
    //TODO3: functions for user approval of block or override

    //TODO4: set blocked plugins as blocked in their URLclassInfos and pass to PluginManager
            //this will stop the plugin from being loaded, 
            //and it will also eliminate the need to do another download to check for names
}