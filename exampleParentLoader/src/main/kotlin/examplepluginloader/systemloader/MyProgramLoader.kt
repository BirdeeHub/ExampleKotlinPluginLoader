package examplepluginloader.systemloader

import java.net.URLClassLoader
import java.io.File
class MyProgramLoader(parentCL: ClassLoader): URLClassLoader(arrayOf(File(File(
    MySystemLoader::class.java.protectionDomain.codeSource.location.toURI())
    .getParent()).toPath().resolve("MyProgram").resolve("examplepluginloader.jar").toUri().toURL()),
    parentCL){
    //im pretty sure this one is just done. It just needs to decend from parentCL and have access to the program's classpath.
}