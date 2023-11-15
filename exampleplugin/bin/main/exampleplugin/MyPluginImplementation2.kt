package exampleplugin
import examplepluginloader.api.MyAPI
import examplepluginloader.api.MyPlugin
import examplepluginloader.api.plugin.PluginUnloadHandler
import java.awt.EventQueue
import java.awt.Frame
import javax.swing.JFrame
import javax.swing.JButton 
import java.awt.Dimension
class MyPluginImplementation2 : MyPlugin{
    override fun launchPlugin(api: MyAPI){
        //normally you call your plugin's opening class from here and pass it the api instance.
        //but this is a test so we just show that making calls to api works.
        println(api.plugin().pluginLocation().toString())
        EventQueue.invokeLater { PluginFrame().setVisible(true) }
        var looptest: Boolean = true
        val thread = Thread {
            try {
                while(looptest){
                    Thread.sleep(2000)
                    print(2)
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
                Frame.getFrames().forEach { frame ->
                    if(frame is PluginFrame){
                        frame.removeAll()
                        frame.dispose()
                    }
                }
            }
        })
        thread.start()
    }
    private class PluginFrame(): JFrame(){
        init{
            getContentPane().setPreferredSize(Dimension(650,530))
            getContentPane().add(JButton("test"))
            pack()
            getContentPane().setVisible(true)
        }
    }
}