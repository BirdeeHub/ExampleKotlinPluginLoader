package examplepluginloader.program
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.apimps.MyAPIobj
import examplepluginloader.Plugger.PluginManager
import java.net.URL
class MyProgram(var pluginPaths: List<String>, mode: Int){
    init{
        println("type s to attempt to load:")
        var aString: String? = ""
        while(aString!="s")aString = readLine()
        println("Testing...")
        var optionalTargets: List<String> = listOf()
        if(pluginPaths.isEmpty()){
            if(mode == 1)pluginPaths=listOf("./plugins/")
            if(mode == 2)pluginPaths=listOf("https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/plugins/examplePlugins.jar")
            //optionalTargets=listOf("exampleplugin.MyPluginImplementation1", "exampleplugin.MyPluginImplementation2")
        } else {
            //optionalTargets=listOf("exampleplugin.MyPluginImplementation1")
            println("Target classes:")
            optionalTargets.forEach { target -> println(target) }
        }
        println("Paths to load from:")
        pluginPaths.forEach { pluginPath -> println(pluginPath) }
        println("Tests:")
        if(mode == 1){
            PluginManager.loadPluginFile(pluginPaths, optionalTargets).forEach {plugID ->
                val plugin: MyPlugin? = PluginManager.getPlugin(plugID)
                if(plugin!=null){
                    println(PluginManager.getPluginLocation(plugID))
                    println(PluginManager.getPluginClassName(plugID))
                    println("UUID: "+PluginManager.getPluginUUID(plugin))
                }
            }
        }
        if(mode == 2){
            PluginManager.loadPluginsFromURLs(pluginPaths.map { URL(it) }, optionalTargets).forEach {plugID ->
                val plugin: MyPlugin? = PluginManager.getPlugin(plugID)
                if(plugin!=null){
                    println(PluginManager.getPluginLocation(plugID))
                    println(PluginManager.getPluginClassName(plugID))
                    println("UUID: "+PluginManager.getPluginUUID(plugin))
                }
            }
        }
        val totalList = PluginManager.getPlugIDList()
        println("All UUIDs: "+PluginManager.getPlugIDList())
        println("type q to attempt to unload:")
        var inputString: String? = ""
        while(inputString!="q")inputString = readLine()
        totalList.forEach { plugID ->
            println("Attempting to Unload: "+" : "+plugID)
            try{
                PluginManager.unloadPlugin(plugID)
            }catch(e: Exception){e.printStackTrace()}
            println("All UUIDs: "+PluginManager.getPlugIDList())
        }
        //PluginManager.unloadAllPlugins()
        println("loaded ${totalList.size} plugin(s)")
        println("type q to attempt to close:")
        var bString: String? = ""
        while(bString!="q")bString = readLine()
        println("Goodbye!")
    }
}