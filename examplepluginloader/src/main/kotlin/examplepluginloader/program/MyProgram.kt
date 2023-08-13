package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.PluggerXP.PluginLoader
class MyProgram(api: MyAPI, var pluginPaths: List<String>){
    init{
        println("Testing...")
        val startTime = System.currentTimeMillis()
        var optionalTargets: List<String> = listOf()
        if(pluginPaths.isEmpty())pluginPaths=listOf("./outputDir/plugins/")
        else {
            optionalTargets=listOf("exampleplugin.MyPluginImplementation1")
            println("Target classes:")
            optionalTargets.forEach { target -> println(target) }
        }
        println("Paths to load from:")
        pluginPaths.forEach { pluginPath -> println(pluginPath) }
        println("Tests:")
        PluginLoader.loadPlugins(api, pluginPaths, optionalTargets).forEach {plugID ->
            val plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
            if(plugin!=null){
                println(plugin.getName()) // MyPluginImplementation loaded
                println(PluginLoader.getPluginLocation(plugID))
                println("UUID: "+PluginLoader.getPluginUUID(plugin))
            }
        }
        val totalnumber: Int = PluginLoader.getPlugIDList().size
        println("All UUIDs: "+PluginLoader.getPlugIDList())
        PluginLoader.getPlugIDList().forEach { plugID ->
            println("Unloading: "+PluginLoader.getPlugin(plugID)?.getName()+" : "+plugID)
            PluginLoader.unloadPlugin(plugID)
            println("All UUIDs: "+PluginLoader.getPlugIDList())
        }
        println("loaded and unloaded $totalnumber plugin(s)")
        println("duration in milliseconds: "+(System.currentTimeMillis() - startTime).toString())
        println("Goodbye!")
    }
}