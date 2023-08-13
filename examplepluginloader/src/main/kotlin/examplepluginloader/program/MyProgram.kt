package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.PluggerXP.PluginLoader
class MyProgram(api: MyAPI, var pluginPaths: Array<String>){
    init{
        println("Testing...")
        val startTime = System.currentTimeMillis()
        var targets: Array<String> = arrayOf()
        if(pluginPaths.isEmpty()){
            pluginPaths=arrayOf("./outputDir/plugins/")
        }else {
                targets=arrayOf("exampleplugin.MyPluginImplementation1")
                println("target classes:")
                targets.forEach { target -> println(target) }
        }
        println("paths to load from:")
        pluginPaths.forEach { pluginPath -> println(pluginPath) }
        println("tests:")
        // targets is optional if you dont want to specify. default value = arrayOf()
        PluginLoader.callPlugLoader(api, pluginPaths, targets).forEach {plugID ->
            var plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
            if(plugin!=null){
                println(plugin.getName()) // MyPluginImplementation loaded
                println(PluginLoader.getPluginLocation(plugID))
                println("UUID: "+PluginLoader.getPluginUUID(plugin))
            }
        }
        val totalnumber: Int = PluginLoader.getPlugIDList().size
        println("All UUIDs: "+PluginLoader.getPlugIDList())
        PluginLoader.getPlugIDList().forEach { plugID ->
            println("unloading: "+PluginLoader.getPlugin(plugID)?.getName()+" : "+plugID)
            PluginLoader.unloadPlugin(plugID)
            println("All UUIDs: "+PluginLoader.getPlugIDList())
        }
        println("loaded and unloaded $totalnumber plugin(s)")
        println("duration in milliseconds: "+(System.currentTimeMillis() - startTime).toString())
        println("Goodbye!")
    }
}