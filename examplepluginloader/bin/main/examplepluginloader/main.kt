package examplepluginloader
import examplepluginloader.api.MyPlugin
import examplepluginloader.api.PluggerXP.MyAPIobj
import examplepluginloader.program.MyProgram
fun main(args: Array<String>) {
    if(args.isEmpty())MyProgram(MyAPIobj(), arrayOf("./outputDir/plugins/"))
    else MyProgram(MyAPIobj(), args)
}
