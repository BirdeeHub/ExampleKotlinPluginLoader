package examplepluginloader.PluggerXP

import examplepluginloader.systemloader.MySystemLoader
import java.net.URL
import java.util.UUID
class PluginLoader(val plugURL: URL, val plugID: UUID): 
ClassLoader(PluginManager::class.java.classLoader.parent) { //PluginLoader is running under MyProgramLoader. Get parent, which is MySystemLoader
    //this classloader should not load for itself. That way we can cut it off. Pass URL to MySystemLoader
    init{(parent as MySystemLoader).addPluginURLs(listOf(plugURL))}
    fun getUUID()=plugID
    fun close(){/* TO DO: Make this tell MySystemLoader our UUID and to lock us down */}
    //TO DO: 
    //change load class, find resource, find resources, find class, etc to instead call special versions in systemclassloader and provide UUID of plugin as argument
    //these versions must not be able to delegate straight to the bootloader, instead they must go through the parent loader
    //that way, MySystemLoader can prevent all ability for this loader to access resources
    //make close notify parent that it has been closed for shutdown sequence
}