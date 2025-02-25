**A plugin loader example program in kotlin-jvm**

It should be pretty readable for Java coders. 

It is part of another project that did not get finished.

It didn't finish the other project because, instead of doing this, I should have used OSGi, which I didn't know about until after I had started. That way I wouldn't have spent a few weeks doing this instead.

It's JUST the plugin loader, and it has a jar for the entrypoint, a jar for the api that can be imported by the plugin, and a jar for program with the plugin loader itself.

It also has a couple plugins for testing, one of which is [minesweeper but loaded as a plugin](https://github.com/BirdeeHub/ExampleKotlinPluginLoader/blob/main/MinesweeperPlugin/src/main/java/MySweep/PluginLoading.java) from [this repo](https://github.com/BirdeeHub/minesweeper)

gradle is used for build, so just ```gradle build shadowJar```

Default plugin directory when you run it assumes you are at project root but you can specify.

```java -jar ExampleOut/entryPoint.jar 1 <optional path to plugins>```

------

Main is in entryPoint:

[Main.kt](https://github.com/BirdeeHub/ExampleKotlinPluginLoader/blob/main/entryPoint/src/main/kotlin/examplepluginloader/main.kt) sets up the shared parent which has the api in its classpath. 

It is parent for both the program's loader and the plugin loaders. 

Main.kt then launches the program with the program loader.

-------

Minimal API in [exampleAPI](https://github.com/BirdeeHub/ExampleKotlinPluginLoader/blob/main/exampleAPI/src/main/kotlin/examplepluginloader/api/MyAPI.kt):

The only api functions of note are a shutdown hook, and the interface to implement to make a plugin. 

The hook from the user is passed to PluginManager in a "plugistration" implementation

```java
public class PluginLoading implements MyPlugin {
    public void launchPlugin(MyAPI api){
        // do plugin stuff
        api.plugin().registerShutdownSequence(new PluginUnloadHandler(){
            public void pluginUnloaded(){
                // cleanup code if needed
            }
        });
    }
}
```

```kotlin
public interface ManagePlugins{
    fun pluginLocation(): URL?
    fun unloadPlugin()
    fun registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration
}
```

--------

Main logic in examplepluginloader:

PluginLoader.kt is the loader for the plugins, 1 new one is created for each one.

PluginManager manages the instances and has the functions for opening and closing

MyProgram calls functions from PluginManager, and prints output.

side note:

JByteCodeURLINFO.kt just gets class names in a jar. Top 45 lines of that file should be all you need for debug. 

As far as PluginManager is concerned it just finds class names of plugins.

It does not hold any references to the active plugin classes themselves and all input streams are closed. 

The parts of note are at the following locations:

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/tree/main/examplepluginloader/src/main/kotlin/examplepluginloader/Plugger/PluginLoader.kt

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/tree/main/examplepluginloader/src/main/kotlin/examplepluginloader/Plugger/PluginManager.kt
