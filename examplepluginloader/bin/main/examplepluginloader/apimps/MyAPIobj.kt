package examplepluginloader.apimps
import examplepluginloader.api.MyAPI
import examplepluginloader.api.plugin.ManagePlugins
import java.util.UUID
class MyAPIobj(val plugID: UUID) : MyAPI {
    override fun test(): String {return "API call test when launchPlugin(api) is called"}
    override fun plugin(): ManagePlugins = PlugMngrObj(plugID)
}