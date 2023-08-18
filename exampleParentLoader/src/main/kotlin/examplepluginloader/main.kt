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
    val sysloader = MySystemLoader(ClassLoader.getSystemClassLoader())
    val loader = MyProgramLoader(sysloader)
    loader.loadClass("examplepluginloader.program.MyProgram").getConstructor(List::class.java, Int::class.java).newInstance(mutArgs.toList(), mode)
}
