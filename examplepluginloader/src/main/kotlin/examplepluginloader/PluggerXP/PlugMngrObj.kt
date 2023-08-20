package examplepluginloader.PluggerXP
import examplepluginloader.api.plugin.PluginManaging
import examplepluginloader.api.plugin.PluginUnloadHandler
import examplepluginloader.api.plugin.Plugistration
import java.util.UUID
class PlugMngrObj(val plugID: UUID) : PluginManaging {
    override fun registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration {
        PluginManager.registerShutdownHook(plugID, unldHndlr)
        return UnloadPlugistration(plugID)
    }
}