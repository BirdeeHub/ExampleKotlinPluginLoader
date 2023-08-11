package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.PluggerXP.PluginLoader 
class MyProgram(api: MyAPI, pluginPaths: Array<String>){
    init{
        println("Testing...")
        for (pluginPath in pluginPaths) {
            println(pluginPath)
            for(plugID in PluginLoader.callPlugLoader(api, pluginPath)){// 1..
                var plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
                println(plugin?.test())// 2..
                println(plugID)
            }
        }
        println("Goodbye!")
    }
}