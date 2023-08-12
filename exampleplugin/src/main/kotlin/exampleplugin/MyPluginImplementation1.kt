package exampleplugin
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin 
class MyPluginImplementation1 : MyPlugin{
    override fun launchPlugin(api: MyAPI){
        //normally you call your plugin's opening class from here and pass it the api instance.
        println(api.test()) //<-- but this is a test so we just show that making calls to api works.
    }
    override fun getName(): String {
        return "MyPluginImplementation1 getName() Test"
    }
}
