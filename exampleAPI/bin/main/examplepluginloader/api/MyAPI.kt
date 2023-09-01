package examplepluginloader.api
import examplepluginloader.api.plugin.ManagePlugins
public interface MyAPI {
    //this would normally have a lot more things in it.
    fun plugin(): ManagePlugins
}