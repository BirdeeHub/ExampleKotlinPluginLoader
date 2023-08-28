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
    //TODO: Use Info from jByteCodeInfo.dangerScan() to do Danger Check
    //TODO1.1: Composed jByteCodeInfos check
    //TODO1.2: Single jByteCodeInfo check
    //TODO1.3: check classes
    //TODO1.4: check resources
    //TODO2: generate report info
}