package examplepluginloader.systemloader
import java.net.URLClassLoader
import java.net.URL
import java.io.File
class MySystemLoader(parentCL: ClassLoader): URLClassLoader(arrayOf(File(File(
    MySystemLoader::class.java.protectionDomain.codeSource.location.toURI())
    .getParent()).toPath().resolve("API").resolve("exampleAPI.jar").toUri().toURL()),
    parentCL){
    /* this loader has the api in its classpath so that it is accessible to both plugins and the main program 
    Sorry about the conversion gore above, but it gives uri in jarfile form at first......*/
}