package examplepluginloader.systemloader

import java.net.URLClassLoader
import java.io.File
class MySystemLoader(parentCL: ClassLoader): URLClassLoader(arrayOf(File(File(
    MySystemLoader::class.java.protectionDomain.codeSource.location.toURI())
    .getParent()).toPath().resolve("API").resolve("exampleAPI.jar").toUri().toURL()),
    parentCL){
        
}