package examplepluginloader.systemloader
import java.net.URLClassLoader
import java.net.URL
import java.io.File
class MySystemLoader(apiLocation: URL, parentCL: ClassLoader): 
    URLClassLoader(arrayOf(apiLocation),parentCL){}