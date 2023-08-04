package exampleplugin

import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin 
class MyPluginImplementation : MyPlugin{
    lateinit var myapi: MyAPI
    override fun launchPlugin(api: MyAPI){
        myapi=api
    }
    override fun test(): String {
        return myapi.test()
    }
}
