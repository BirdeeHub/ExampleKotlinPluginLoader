package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.PluggerXP.PluginLoader 
class MyProgram(api: MyAPI, pluginPaths: Array<String>){
    init{
        println("Testing...")
        pluginPaths.forEach { pluginPath -> println(pluginPath) }
        for(plugID in PluginLoader.callPlugLoader(api, pluginPaths)){// Test 1..
            var plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
            println(plugin?.getName())// MyPluginImplementation loaded
            println(plugID)
        }
        println(PluginLoader.getPlugIDList())
        PluginLoader.unloadAllPlugins() // in this case, not necessary because the jvm closes when our program closes. but still good practice
        println("Goodbye!")
    }
}