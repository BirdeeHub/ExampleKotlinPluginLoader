package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.api.PluggerXP.MyAPIobj
import examplepluginloader.PluggerXP.PluginLoader
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
            PluginLoader.loadPluginFile(MyAPIobj(), pluginPaths, optionalTargets).forEach {plugID ->
                val plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
                if(plugin!=null){
                    println(plugin.getName()) // MyPluginImplementation loaded
                    println(PluginLoader.getPluginLocation(plugID))
                    println("UUID: "+PluginLoader.getPluginUUID(plugin))
                }
            }
        }
        if(mode == 2){
            PluginLoader.loadPluginsFromURLs(MyAPIobj(), pluginPaths.map { URL(it) }, optionalTargets).forEach {plugID ->
                val plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
                if(plugin!=null){
                    println(plugin.getName()) // MyPluginImplementation loaded
                    println(PluginLoader.getPluginLocation(plugID))
                    println("UUID: "+PluginLoader.getPluginUUID(plugin))
                }
            }
        }
        val totalnumber: Int = PluginLoader.getPlugIDList().size
        println("All UUIDs: "+PluginLoader.getPlugIDList())
        println("type q to attempt to close:")
        var inputString: String? = ""
        while(inputString!="q")inputString = readLine()
        PluginLoader.getPlugIDList().forEach { plugID ->
            println("Attempting to Unload: "+PluginLoader.getPlugin(plugID)?.getName()+" : "+plugID)
            PluginLoader.unloadPlugin(plugID)
            println("All UUIDs: "+PluginLoader.getPlugIDList())
        }
        println("loaded $totalnumber plugin(s)")
        println("Goodbye!")
    }
}