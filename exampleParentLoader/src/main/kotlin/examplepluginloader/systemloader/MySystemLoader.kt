package examplepluginloader.systemloader
import java.net.URLClassLoader
import java.io.File
class MySystemLoader(parentCL: ClassLoader): URLClassLoader(arrayOf(File(File(
    MySystemLoader::class.java.protectionDomain.codeSource.location.toURI())
    .getParent()).toPath().resolve("API").resolve("exampleAPI.jar").toUri().toURL()),
    parentCL){
    //To Do: make a separate set of find resource, resources, class, etc for PluginClassLoader that can block access by UUID, which gets supplied as argument,
    //and also make a function that takes in a uuid and switches the blocking on for that UUID
}