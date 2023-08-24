package examplepluginloader.Plugger

import java.net.URLClassLoader
import java.net.URL
import java.io.InputStream
import java.util.UUID
import java.util.Enumeration
class PluginLoader(val plugURL: URL, val plugID: UUID, val parentCL: ClassLoader): URLClassLoader(arrayOf(plugURL), parentCL){
    //this loader actually loads the stuff for the plugin, and can cut it off.
    private var pluginIsLoaded = true
    fun getUUID()=plugID
    fun addPluginURLs(pluginURLs: List<URL>) = pluginURLs.forEach { plugURL -> this.addURL(plugURL) }
    override fun close(){
        this.setDefaultAssertionStatus(false)
        this.clearAssertionStatus()
        super.close()
        System.gc()
        try{
            Thread.sleep(500)
        }catch(e: InterruptedException){}
        pluginIsLoaded=false
    }
    override protected fun findClass(name: String): Class<*> {
        if(pluginIsLoaded) return super.findClass(name)
        else throw ClassNotFoundException(name)
    }
    override fun loadClass(name: String): Class<*> {
        if(pluginIsLoaded) return super.loadClass(name)
        else throw ClassNotFoundException(name)
    }
    override fun findResource(name: String): URL {
        if(pluginIsLoaded) return super.findResource(name)
        else throw ClassNotFoundException(name)
    }
    override fun getResource(name: String): URL {
        if(pluginIsLoaded) return super.getResource(name)
        else throw ClassNotFoundException(name)
    }
    override fun getResourceAsStream(name: String): InputStream {
        if(pluginIsLoaded) return super.getResourceAsStream(name)
        else throw ClassNotFoundException(name)
    }
    override fun getResources(name: String): Enumeration<URL> {
        if(pluginIsLoaded) return super.getResources(name)
        else throw ClassNotFoundException(name)
    }
}