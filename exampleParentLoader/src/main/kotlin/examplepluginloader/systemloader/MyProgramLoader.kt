package examplepluginloader.systemloader
import java.net.URL
import java.net.URLClassLoader
import java.io.File
class MyProgramLoader(programLocation: URL, parentCL: ClassLoader): 
    URLClassLoader(arrayOf(programLocation),parentCL){}