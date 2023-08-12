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
            targets=arrayOf("exampleplugin.MyPluginImplementation1")
        }
        pluginPaths.forEach { pluginPath -> println(pluginPath) }
        for(plugID in PluginLoader.callPlugLoader(api, pluginPaths, targets)){ // package name optional if not in package
            var plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
            if(plugin!=null){
                println(plugin.getName()) // MyPluginImplementation loaded
                println("UUID: "+PluginLoader.getPluginUUID(plugin))
            }
        }
        val totalnumber: Int = PluginLoader.getPlugIDList().size
        println("All UUIDs: "+PluginLoader.getPlugIDList())
        for(plugID in PluginLoader.getPlugIDList()){
            println("unloading: "+PluginLoader.getPlugin(plugID)?.getName()+" : "+plugID)
            PluginLoader.unloadPlugin(plugID)
            println("All UUIDs: "+PluginLoader.getPlugIDList())
        }
        println("loaded and unloaded $totalnumber plugin(s)")
        println("duration in milliseconds: "+(System.currentTimeMillis() - startTime).toString())
        println("Goodbye!")
    }
}