package exampleplugin

import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin 
class MyPluginImplementation : MyPlugin{
    override fun launchPlugin(api: MyAPI){
        println(api.test())
    }
    override fun test(): String {
        return "2..."
    }
}
