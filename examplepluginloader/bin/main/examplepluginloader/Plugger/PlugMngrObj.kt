package examplepluginloader.Plugger
import examplepluginloader.api.plugin.ManagePlugins
import examplepluginloader.api.plugin.PluginUnloadHandler
import examplepluginloader.api.plugin.Plugistration
import java.util.UUID
class PlugMngrObj(val plugID: UUID) : ManagePlugins {
    override fun registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration =
        PluginManager.registerShutdownHook(plugID, unldHndlr)
}