package examplepluginloader.PluggerXP
import examplepluginloader.api.plugin.Plugistration
import java.util.UUID
class UnloadPlugistration(val plugID: UUID): Plugistration{
    override fun isRegistered(): Boolean = PluginManager.shudownRegistered(plugID)
    override fun deregister() { PluginManager.shutdownderegister(plugID) }
}