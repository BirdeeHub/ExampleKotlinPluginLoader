package examplepluginloader.api.PluggerXP
import examplepluginloader.api.MyAPI
class MyAPIobj : MyAPI {
    override fun test(): String {return "API call test when launchPlugin(api) is called"}
}