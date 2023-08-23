package examplepluginloader
import examplepluginloader.systemloader.MySystemLoader
import examplepluginloader.systemloader.MyProgramLoader
import kotlin.collections.remove
import java.io.File
import java.lang.reflect.Type
fun main(args: Array<String>) { 
    val mutArgs = args.toMutableList()
    var mode :Int
    try{
        mode = Integer.parseInt(args[0])
        mutArgs.removeAt(0)
    } catch (e: Exception){ mode = 1 }
    val sysloader = MySystemLoader(File(File(
        MySystemLoader::class.java.protectionDomain.codeSource.location.toURI())
        .getParent()).toPath().resolve("API").resolve("exampleAPI.jar").toUri().toURL(), ClassLoader.getSystemClassLoader())
    val loader = MyProgramLoader(File(File(
        MySystemLoader::class.java.protectionDomain.codeSource.location.toURI())
        .getParent()).toPath().resolve("MyProgram").resolve("examplepluginloader.jar").toUri().toURL(), sysloader)
    loader.loadClass("examplepluginloader.program.MyProgram").getConstructor(List::class.java, Int::class.java).newInstance(mutArgs.toList(), mode)
}
