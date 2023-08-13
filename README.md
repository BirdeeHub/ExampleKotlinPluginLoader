**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

You will need a copy of JDK 17. The kotlin runtime is in the jar.

It can load any number of classes that implement MyPlugin that are defined inside the first package or (no package) of .jar and .class files.

It can load them from file or directory and they can be in java or kotlin, and you can specify package.class names if you want.

It can also close them individually or all at once, and contains various useful reference functions

For optimum performance, do not load too many directories if they may have unrelated .jar or .class files in them.

It was a tiny part of another program but it came out so nicely I figured it could be useful to people before I finish the other project.

I then built a quick mockup test for it to upload it, in case someone finds it useful

**The following is the link to the actual loader class:**

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

**Instructions:**

if you wish to use, you probably only need the thing i linked.

**To run the quick mockup:**

1st: go to root directory of the git repo.

Build first with: ```gradle clean build shadowJar``` (if desired)

to run:

stay in the same project root directory and run the following command:

```java -jar ./outputDir/examplepluginloader-all.jar```

default runs plugins from ./outputDir, so if you go to outputDir on the command line you will need to run the following

```java -jar ./examplepluginloader-all.jar ./plugins/```

the expected output is the following:

for ```java -jar ./outputDir/examplepluginloader-all.jar```:
```
Testing...
Paths to load from:
./outputDir/plugins/
Tests:
API call test when launchPlugin(api) is called (This gets filtered out if arguments are used for demonstration)
API call test when launchPlugin(api) is called
MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration)
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: 0f74addf-41e8-431f-9622-54db1c62555c
MyPluginImplementation1 getName() Test
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: 81719d41-fc82-4650-9794-692492e0d672
All UUIDs: [0f74addf-41e8-431f-9622-54db1c62555c, 81719d41-fc82-4650-9794-692492e0d672]
Unloading: MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration) : 0f74addf-41e8-431f-9622-54db1c62555c
All UUIDs: [81719d41-fc82-4650-9794-692492e0d672]
Unloading: MyPluginImplementation1 getName() Test : 81719d41-fc82-4650-9794-692492e0d672
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 312
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/plugins/ ./outputDir/plugins/exampleplugin.jar```:
```
Testing...
Target classes:
exampleplugin.MyPluginImplementation1
Paths to load from:
./outputDir/plugins/
./outputDir/plugins/exampleplugin.jar
Tests:
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
MyPluginImplementation1 getName() Test
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: 42b1880f-5683-4531-bc77-18a490286c25
MyPluginImplementation1 getName() Test
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: eca18165-38b6-425e-ade6-fb4086214cb4
All UUIDs: [42b1880f-5683-4531-bc77-18a490286c25, eca18165-38b6-425e-ade6-fb4086214cb4]
Unloading: MyPluginImplementation1 getName() Test : 42b1880f-5683-4531-bc77-18a490286c25
All UUIDs: [eca18165-38b6-425e-ade6-fb4086214cb4]
Unloading: MyPluginImplementation1 getName() Test : eca18165-38b6-425e-ade6-fb4086214cb4
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 308
Goodbye!
```

I'm pretty sure that if you want to load from web, you only need to change the following functions,

and then make a loadPluginsFrom_protocol_Location(plugURL: URL, targetClassNames: List<String>): MutableList<UUID> 

which would find the classes at the url, because reflections doesnt seem to work over http

```kotlin
    private fun loadPluginsFromGenLocation(pluginURI: URI, targetClassNames: List<String>): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        try{
            for(plugURL in getJarURLs(pluginURI)){ //<-- getJarURLs(pluginPath: File): List<URL>
                if(plugURL.protocol == "file")plugIDs.addAll(loadPluginsFromFileLocation(plugURL, targetClassNames))
            }
        }catch(e: Exception){e.printStackTrace()}
        return plugIDs //<-- returns the uuids of the new plugins loaded
    }
    private fun getJarURLs(pluginPathURI: URI): List<URL> {
        val pluginPath: URL
        pluginPath = pluginPathURI.toURL()
        if(pluginPath.protocol == "file"){
            if(File(pluginPathURI).isDirectory()){
                // get all jar files in the directory and convert list to a mutable list so we can add any .class files, then add those too
                val bytecodefiles = (File(pluginPathURI).listFiles { file -> file.name.endsWith(".jar") }
                    .map { it.toURI().toURL() }).toMutableList()
                bytecodefiles.addAll(File(pluginPathURI).listFiles { file -> file.name.endsWith(".class") }.map { it.toURI().toURL() })
                return bytecodefiles
            } else return listOf(pluginPath) //<-- else if specific file was specified, return the url as a 1 element list
        } else return listOf()
    }
    private fun loadPluginsFromFileLocation(plugURL: URL, targetClassNames: List<String>): MutableList<UUID> {
        val plugIDs = mutableListOf<UUID>()
        // Get all subtypes of MyPlugin using Reflections (Which I cant get to work over the internet)
        val reflections = Reflections(ConfigurationBuilder().addUrls(plugURL)
            .addClassLoaders(URLClassLoader(arrayOf(plugURL), PluginLoader::class.java.classLoader)))
        var pluginClasses = reflections.getSubTypesOf(MyPlugin::class.java).toList()
        //now that we have the classes, convert them to KClasses, and filter then load them
        plugIDs.addAll(loadPluginKClassesFromURLFilePath(pluginClasses.map { it.kotlin }, plugURL, targetClassNames))
        return plugIDs
    }
```