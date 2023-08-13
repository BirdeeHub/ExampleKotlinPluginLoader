package examplepluginloader
import examplepluginloader.api.PluggerXP.MyAPIobj
import examplepluginloader.program.MyProgram
fun main(args: Array<String>) { MyProgram(MyAPIobj(), args.toList()) }
