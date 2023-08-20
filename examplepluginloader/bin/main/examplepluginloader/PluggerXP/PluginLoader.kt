package examplepluginloader.PluggerXP

import java.net.URLClassLoader
import java.net.URL
import java.io.InputStream
import java.util.UUID
import java.util.Enumeration
class PluginLoader(val plugURL: URL, val plugID: UUID, val parentCL: ClassLoader): URLClassLoader(arrayOf(plugURL), parentCL){
    //this loader actually loads the stuff for the plugin, and can cut it off.
    private var pluginIsLoaded = true
    val pluginCLoader = PluginClassLoader(plugID, this)
    fun getUUID()=plugID
    fun addPluginURLs(pluginURLs: List<URL>) = pluginURLs.forEach { plugURL -> this.addURL(plugURL) }
    override fun close(){
        pluginIsLoaded=false
        super.close()
    }
    fun findPluginClass(name: String): Class<*> {
        if(pluginIsLoaded) return findClass(name)
        else throw ClassNotFoundException(name)
    }
    fun loadPluginClass(name: String): Class<*>{
        if(pluginIsLoaded) return loadClass(name)
        else throw ClassNotFoundException(name)
    }
    fun findPluginResource(name: String): URL{
        if(pluginIsLoaded) return findResource(name)
        else throw ClassNotFoundException(name)
    }
    fun getPluginResource(name: String): URL{
        if(pluginIsLoaded) return getResource(name)
        else throw ClassNotFoundException(name)
    }
    fun getPluginResourceAsStream(name: String): InputStream {
        if(pluginIsLoaded) return getResourceAsStream(name)
        else throw ClassNotFoundException(name)
    }
    fun getPluginResources(name: String): Enumeration<URL>{
        if(pluginIsLoaded) return getResources(name)
        else throw ClassNotFoundException(name)
    }
    //this classloader should not load for itself. That way we can cut it off.
    class PluginClassLoader(val plugID: UUID, parentCL: ClassLoader): 
    ClassLoader(parentCL) { 
        fun addPluginURLs(plugURLs: List<URL>) = 
            (parent as PluginLoader).addPluginURLs(plugURLs)
        fun getUUID()=plugID
        override protected fun findClass(name: String): Class<*> { 
            return (parent as PluginLoader).findPluginClass(name)
        }
        override fun loadClass(name: String): Class<*> { 
            return (parent as PluginLoader).loadPluginClass(name)
        }
        override fun findResource(name: String): URL{
            return (parent as PluginLoader).findPluginResource(name)
        }
        override fun getResource(name: String): URL{
            return (parent as PluginLoader).getPluginResource(name)
        }
        override fun getResourceAsStream(name: String): InputStream {
            return (parent as PluginLoader).getPluginResourceAsStream(name)
        }
        override fun getResources(name: String): Enumeration<URL> {
            return (parent as PluginLoader).getPluginResources(name)
        }
    }
}