package examplepluginloader.Plugger
import examplepluginloader.api.plugin.Plugistration
import examplepluginloader.api.plugin.PluginUnloadHandler
import java.util.UUID
class UnloadPlugistration(val plugID: UUID, val unldHndlr: PluginUnloadHandler): Plugistration{
    override fun isRegistered(): Boolean = PluginManager.shudownRegistered(this)
    override fun deregister() { PluginManager.shutdownderegister(this) }
}