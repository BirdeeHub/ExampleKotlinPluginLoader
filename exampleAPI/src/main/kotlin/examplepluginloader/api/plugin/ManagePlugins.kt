package examplepluginloader.api.plugin
public interface ManagePlugins{
    fun registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration
}