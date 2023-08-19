package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.api.PluggerXP.MyAPIobj
import examplepluginloader.PluggerXP.PluginManager
import java.net.URL
class MyProgram(var pluginPaths: List<String>, mode: Int){
    init{
        println("Testing...")
        var optionalTargets: List<String> = listOf()
        if(pluginPaths.isEmpty()){
            if(mode == 1)pluginPaths=listOf("./outputDir/plugins/")
            if(mode == 2)pluginPaths=listOf("https://github.com/BirdeeHub/minesweeper/raw/NotATutorial/app/minesweeper.jar")
        } else {
            optionalTargets=listOf("exampleplugin.MyPluginImplementation1")
            println("Target classes:")
            optionalTargets.forEach { target -> println(target) }
        }
        println("Paths to load from:")
        pluginPaths.forEach { pluginPath -> println(pluginPath) }
        println("Tests:")
        if(mode == 1){
            PluginManager.loadPluginFile(MyAPIobj(), pluginPaths, optionalTargets).forEach {plugID ->
                val plugin: MyPlugin? = PluginManager.getPlugin(plugID)
                if(plugin!=null){
                    println(plugin.getName()) // MyPluginImplementation loaded
                    println(PluginManager.getPluginLocation(plugID))
                    println(PluginManager.getPluginClassName(plugID))
                    println("UUID: "+PluginManager.getPluginUUID(plugin))
                }
            }
        }
        if(mode == 2){
            PluginManager.loadPluginsFromURLs(MyAPIobj(), pluginPaths.map { URL(it) }, optionalTargets).forEach {plugID ->
                val plugin: MyPlugin? = PluginManager.getPlugin(plugID)
                if(plugin!=null){
                    println(plugin.getName()) // MyPluginImplementation loaded
                    println(PluginManager.getPluginLocation(plugID))
                    println(PluginManager.getPluginClassName(plugID))
                    println("UUID: "+PluginManager.getPluginUUID(plugin))
                }
            }
        }
        val totalnumber: Int = PluginManager.getPlugIDList().size
        println("All UUIDs: "+PluginManager.getPlugIDList())
        println("type q to attempt to close:")
        var inputString: String? = ""
        while(inputString!="q")inputString = readLine()
        PluginManager.getPlugIDList().forEach { plugID ->
            println("Attempting to Unload: "+PluginManager.getPlugin(plugID)?.getName()+" : "+plugID)
            PluginManager.unloadPlugin(plugID)
            println("All UUIDs: "+PluginManager.getPlugIDList())
        }
        println("loaded $totalnumber plugin(s)")
        println("Goodbye!")
    }
}