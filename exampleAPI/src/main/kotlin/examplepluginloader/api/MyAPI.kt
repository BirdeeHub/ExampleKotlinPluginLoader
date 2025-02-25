package examplepluginloader.api
import examplepluginloader.api.plugin.ManagePlugins
public interface MyAPI {
    //this would normally have a lot more things in it.
    // the api you want to expose to plugins will go here
    // but right now it only has plugin lifecycle stuff
    fun plugin(): ManagePlugins
}
