package exampleplugin
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.api.plugin.PluginUnloadHandler 
class MyPluginImplementation1 : MyPlugin{
    override fun launchPlugin(api: MyAPI){
        //normally you call your plugin's opening class from here and pass it the api instance.
        println(api.plugin().pluginLocation().toString()) //<-- but this is a test so we just show that making calls to api works.

        var looptest: Boolean = true
        val thread = Thread {
            try {
                while(looptest){
                    Thread.sleep(2000)
                    print(1)
                }
                println("thread finished!")
            } catch (e: InterruptedException) {
                println("Thread was interrupted!")
            }
        }
        api.plugin().registerShutdownSequence(object: PluginUnloadHandler{
            override fun pluginUnloaded() {
                looptest = false
                thread.interrupt()
            }
        })
        thread.start()
    }
}
