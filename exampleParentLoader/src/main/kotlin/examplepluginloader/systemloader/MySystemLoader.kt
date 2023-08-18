package examplepluginloader.systemloader
import java.net.URLClassLoader
import java.net.URL
import java.io.File
class MySystemLoader(parentCL: ClassLoader): URLClassLoader(arrayOf(File(File(
    MySystemLoader::class.java.protectionDomain.codeSource.location.toURI())
    .getParent()).toPath().resolve("API").resolve("exampleAPI.jar").toUri().toURL()),
    parentCL){
    //plugin is a basic classloader and cannot load URLs. We also want to own the classpath. So heres a function to get them from it.
    fun addPluginURLs(pluginURLs: List<URL>) = pluginURLs.forEach { plugURL -> this.addURL(plugURL) }
    //To Do: make a separate set of find resource, resources, class, etc for PluginClassLoader that can block access by UUID, which gets supplied as argument,
    //and also make a function that takes in a uuid and switches the blocking on for that UUID
}