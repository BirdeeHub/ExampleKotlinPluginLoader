package examplepluginloader.api
import examplepluginloader.api.plugin.ManagePlugins
public interface MyAPI {
    fun test() : String
    //this would normally have a lot more things in it.
    fun plugin(): ManagePlugins
}