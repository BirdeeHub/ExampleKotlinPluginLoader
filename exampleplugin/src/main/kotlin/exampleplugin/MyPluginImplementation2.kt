package exampleplugin
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin 
class MyPluginImplementation2 : MyPlugin{
    override fun launchPlugin(api: MyAPI){
        //normally you call your plugin's opening class from here and pass it the api instance.
        //but this is a test so we just show that making calls to api works.
        println(api.test()+" (entering paths will set target to only plugin 1, removing this)") 
    }
    override fun getName(): String {
        return "MyPluginImplementation2 getName() Test (Filtering Demo)"
    }
}
