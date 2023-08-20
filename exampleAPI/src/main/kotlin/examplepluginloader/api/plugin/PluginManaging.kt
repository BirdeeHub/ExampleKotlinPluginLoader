package examplepluginloader.api.plugin
public interface PluginManaging{
    fun registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration
}