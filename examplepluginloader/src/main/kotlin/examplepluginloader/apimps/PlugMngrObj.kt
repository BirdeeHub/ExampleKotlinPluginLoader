package examplepluginloader.apimps
import examplepluginloader.api.plugin.ManagePlugins
import examplepluginloader.api.plugin.PluginUnloadHandler
import examplepluginloader.api.plugin.Plugistration
import examplepluginloader.Plugger.PluginManager
import java.util.UUID
import java.net.URL
class PlugMngrObj(val plugID: UUID) : ManagePlugins {
    override fun pluginLocation(): URL? = 
        PluginManager.getPluginLocation(plugID)
    override fun unloadPlugin() = 
        PluginManager.unloadPlugin(plugID)
    override fun registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration =
        PluginManager.registerShutdownHook(plugID, unldHndlr)
}