package exampleplugin
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin 
class MyPluginImplementation2 : MyPlugin{
    override fun launchPlugin(api: MyAPI){
        //normally you call your plugin's opening class from here and pass it the api instance.
        println(api.test()+" (This gets filtered out if arguments are used for demonstration)") //<-- but this is a test so we just show that making calls to api works.
    }
    override fun getName(): String {
        return "MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration)"
    }
}
