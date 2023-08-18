package examplepluginloader.api
import examplepluginloader.api.MyAPI
public interface MyPlugin {
    fun launchPlugin(api: MyAPI)
    fun getName(): String
    //this one probably wouldnt have anything else.
}