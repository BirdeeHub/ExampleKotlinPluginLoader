package examplepluginloader.api
import examplepluginloader.api.MyAPI
public interface MyPlugin {
    fun launchPlugin(api: MyAPI)
    fun test(): String
    //this one probably wouldnt have anything else. It probably wouldn't even have test()
}