package examplepluginloader.api.plugin
public interface Plugistration {
    fun isRegistered(): Boolean
    fun deregister()
}