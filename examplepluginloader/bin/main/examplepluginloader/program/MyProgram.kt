package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.PluggerXP.PluginLoader 
class MyProgram(api: MyAPI, pluginPath: String){
    init{
        var plugIDs = PluginLoader.callPlugLoader(api, pluginPath)
        for(plugID in plugIDs){
            var plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
            println("test")
            println(plugin?.test())
            println(plugID)
        }
    }
}