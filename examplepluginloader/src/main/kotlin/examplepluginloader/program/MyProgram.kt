package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import java.util.UUID
import examplepluginloader.PluggerXP.PluginLoader 
class MyProgram(api: MyAPI, pluginPaths: Array<String>){
    init{
        println("Testing...")
        val plugIDs: MutableList<UUID> = mutableListOf()
        for (pluginPath in pluginPaths) plugIDs.addAll(PluginLoader.callPlugLoader(api, pluginPath))
        for(plugID in plugIDs){
            var plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
            println(plugin?.test())
            println(plugID)
        }
        println("Goodbye!")
    }
}