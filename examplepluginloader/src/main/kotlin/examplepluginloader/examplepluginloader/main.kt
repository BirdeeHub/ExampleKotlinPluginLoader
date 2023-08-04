package examplepluginloader
import examplepluginloader.api.MyPlugin
import examplepluginloader.api.MyAPI
import examplepluginloader.api.PluggerXP.MyAPIobj
import examplepluginloader.program.MyProgram
fun main(args: Array<String>) {
    MyProgram(MyAPIobj(), args[0])
}
