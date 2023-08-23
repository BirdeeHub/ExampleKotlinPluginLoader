package examplepluginloader.apimps
import examplepluginloader.api.MyAPI
import examplepluginloader.api.plugin.ManagePlugins
import java.util.UUID
class MyAPIobj(val plugID: UUID) : MyAPI {
    override fun plugin(): ManagePlugins = PlugMngrObj(plugID)
}