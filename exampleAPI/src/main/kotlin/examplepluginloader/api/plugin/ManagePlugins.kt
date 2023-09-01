package examplepluginloader.api.plugin
import java.net.URL
public interface ManagePlugins{
    fun pluginLocation(): URL?
    fun unloadPlugin()
    fun registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration
}