package examplepluginloader
import examplepluginloader.api.PluggerXP.MyAPIobj
import examplepluginloader.program.MyProgram
import kotlin.collections.remove
fun main(args: Array<String>) { 
    val mutArgs = args.toMutableList()
    var mode :Int
    try{
        mode = Integer.parseInt(args[0])
        mutArgs.removeAt(0)
    } catch (e: Exception){ mode = 1 }
    MyProgram(MyAPIobj(), mutArgs, mode)
}
