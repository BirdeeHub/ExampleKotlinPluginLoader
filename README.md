**A plugin loader example program in kotlin-jvm**

It should be pretty readable for Java coders. 

gradle is used for build, so just ```gradle build shadowJar```

Default plugin directory when you run it assumes you are at project root but you can specify.

```java -jar ExampleOut/entryPoint.jar 1 <optional path to plugins>```

----------------------------------------------------------------------------------------------

I learned to use a profile better, and also changed a couple more things, 

and now it unloads everything except for classloaders that held JFrames that contain components.

There are no longer any active instances other than the loader with the mentioned JFrame itself. 

It closes the other stuff, including loaders that dont create frames with swing components with events in them.

I.E. it will clear an empty JFrame and its classloader, but if you put stuff inside it, it chokes up the unload.
(even if the plugin does frame.removeAll() and frame.dispose() before closing)

Profiler says its some sort of assertion lock but I have no idea how to access it. 

Ive tried clearing assertion status on the classloader on close and a few other things related to cleaning up within the plugin itself

**HELP** Im pretty stuck at this point. I think its due to swing and the EDT somehow, but I am having trouble finding info...

I just want it to release the plugin jar file so that you can edit it without closing the program...

The parts of note are at the following locations:

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/blob/main/entryPoint/src/main/kotlin/examplepluginloader/main.kt

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/tree/main/examplepluginloader/src/main/kotlin/examplepluginloader/Plugger/PluginLoader.kt

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/tree/main/examplepluginloader/src/main/kotlin/examplepluginloader/Plugger/PluginManager.kt

If the ONLY reason is Swing, then I guess it is ok? Im making this as an extension to another program eventually that has functions to manage swing panels. 

But I thought I figured out everything that wasn't swing twice, and I still improved it further, so now I am not sure.

So I either need confirmation that the only remaining issue with unload is swing/EDT, or a suggestion as to how to remove this stupid assertion lock that I did not ask for.

------

Main is in entryPoint:

Main.kt sets up the shared parent which has the api in its classpath. 

It is parent for both the program's loader and the plugin loaders. 

Main.kt then launches the program with the program loader.

-------

Minimal API in exampleAPI:

The only api functions of note are a shutdown hook, and the interface to implement to make a plugin. 

The hook from the user is passed to PluginManager in a "plugistration" implementation

--------

Main logic in examplepluginloader:

PluginLoader.kt is the loader for the plugins, 1 new one is created for each one.

PluginManager manages the instances and has the functions for opening and closing

MyProgram calls functions from PluginManager, and prints output.

side note:

JByteCodeURLINFO.kt just gets class names in a jar. Top 45 lines of that file should be all you need for debug. 

As far as PluginManager is concerned it just finds class names of plugins.

It does not hold any references to the active plugin classes themselves and all input streams are closed. 

--------------------------------------------------------------------------------------------------

This is part of another project and AAAAAAAAAAHHHHHHHHHHH..............

I just want to finally get to write the main features at this point..... 

But I want a working base.... 

And scrapping it all and using OSGi (which I just heard of) sounds so... 

I really really wanted to learn how to do this....
