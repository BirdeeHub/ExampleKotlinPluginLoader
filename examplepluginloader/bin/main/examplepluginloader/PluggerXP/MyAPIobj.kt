package examplepluginloader.PluggerXP
import examplepluginloader.api.MyAPI
import examplepluginloader.api.plugin.PluginManaging
import java.util.UUID
class MyAPIobj(val plugID: UUID) : MyAPI {
    override fun test(): String {return "API call test when launchPlugin(api) is called"}
    override fun plugin(): PluginManaging = PlugMngrObj(plugID)
}