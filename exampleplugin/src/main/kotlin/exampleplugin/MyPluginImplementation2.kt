package exampleplugin
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.api.plugin.PluginUnloadHandler 
class MyPluginImplementation2 : MyPlugin{
    override fun launchPlugin(api: MyAPI){
        //normally you call your plugin's opening class from here and pass it the api instance.
        //but this is a test so we just show that making calls to api works.
        println(api.plugin().pluginLocation().toString()) 
        var looptest: Boolean = true
        val thread = Thread {
            try {
                while(looptest){
                    Thread.sleep(1000)
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
