package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.PluggerXP.PluginLoader 
class MyProgram(api: MyAPI, pluginPaths: Array<String>){
    init{
        println("Testing...")
        pluginPaths.forEach { pluginPath -> println(pluginPath) }
        for(plugID in PluginLoader.callPlugLoader(api, pluginPaths)){ // package name optional.
            var plugin: MyPlugin? = PluginLoader.getPlugin(plugID)
            if(plugin!=null){
                println(plugin.getName()) // MyPluginImplementation loaded
                println("UUID: "+PluginLoader.getPluginUUID(plugin))
            }
        }
        println("All UUIDs: "+PluginLoader.getPlugIDList())
        for(plugID in PluginLoader.getPlugIDList()){
            println("unloading: "+PluginLoader.getPlugin(plugID)?.getName()+" : "+plugID)
            PluginLoader.unloadPlugin(plugID)
            println("All UUIDs: "+PluginLoader.getPlugIDList())
        }
        println("Goodbye!")
    }
}