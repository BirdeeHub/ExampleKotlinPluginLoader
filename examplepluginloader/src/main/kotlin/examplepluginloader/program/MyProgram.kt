package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.PluggerXP.PluginLoader 
class MyProgram(api: MyAPI, pluginPaths: Array<String>){
    init{
        println("Testing...")
        for (pluginPath in pluginPaths) {
            for(plugID in PluginLoader.callPlugLoader(api, pluginPath)){
                println(pluginPath)
                var plugin: MyPlugin? = PluginLoader.getPlugin(plugID) // 1..
                println(plugin?.test())// 2..
                println(plugID)
            }
        }
        println("Goodbye!")
    }
}