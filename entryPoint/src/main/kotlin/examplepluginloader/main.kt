package examplepluginloader
import kotlin.collections.remove
import java.io.File
import java.lang.reflect.Type
import java.net.URLClassLoader
fun main(args: Array<String>) { 
    val mutArgs = args.toMutableList()
    var mode :Int
    try{
        mode = Integer.parseInt(args[0])
        mutArgs.removeAt(0)
    } catch (e: Exception){ mode = 1 }
    val apiPath = File(File(
        FindLocationOfTheThing::class.java.protectionDomain.codeSource.location.toURI())
        .getParent()).toPath().resolve("API").resolve("exampleAPI.jar").toUri().toURL()
    val programLocation = File(File(
        FindLocationOfTheThing::class.java.protectionDomain.codeSource.location.toURI())
        .getParent()).toPath().resolve("MyProgram").resolve("examplepluginloader.jar").toUri().toURL()
    val sysloader = URLClassLoader(arrayOf(apiPath), ClassLoader.getSystemClassLoader())
    val programloader = URLClassLoader(arrayOf(programLocation), sysloader)
    programloader.loadClass("examplepluginloader.program.MyProgram").getConstructor(List::class.java, Int::class.java).newInstance(mutArgs.toList(), mode)
}
private class FindLocationOfTheThing() {}