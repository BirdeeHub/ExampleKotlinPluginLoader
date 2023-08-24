**A plugin loader example program in kotlin-jvm**

It should be pretty readable for Java coders. 

gradle is used for build, so just ```gradle build shadowJar```

Default plugin directory when you run it assumes you are at project root but you can specify.

```java -jar ExampleOut/entryPoint.jar 1 <optional path to plugins>```

----------------------------------------------------------------------------------------------

I learned to use a profile better, and also changed a couple more things, 

and now it unloads everything except for JUST the classloader that was used for minesweeper.

but there are no longer any active instances

**HELP** Im pretty stuck at this point. 

I just want it to release the plugin jar file so that you can edit it without closing the program...

Why is everything now unloading after being dereferenced, EXCEPT for PluginLoader?

The parts of note are at the following locations:

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/blob/main/entryPoint/src/main/kotlin/examplepluginloader/main.kt

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/tree/main/examplepluginloader/src/main/kotlin/examplepluginloader/Plugger/PluginLoader.kt

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/tree/main/examplepluginloader/src/main/kotlin/examplepluginloader/Plugger/PluginManager.kt

-------------------------------------------------------------------------------------

There are only 2 files over about 60 lines. And for one of those, you shouldnt need to look at more than the top 45 lines

Most of them are closer to 10 lines

(other than the java minesweeper game in example plugins, which isnt relevant. The bug happens with or without it.) 

Outline is as follows:

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

JByteCodeURLINFO.kt just gets class names in a jar. Top 45 lines of that file should be all you need.

It should not hold any references to the plugin even when stored in Plugin Manager as it is, and all input streams are closed. 

--------------------------------------------------------------------------------------------------

This is part of another project and AAAAAAAAAAHHHHHHHHHHH..............

I just want to finally get to write the main features at this point..... 

But I want a working base.... 

And scrapping it all and using OSGi (which I just heard of)... 

I really really wanted to learn how to do this....

--------------------------------------------------------------------------------------------------

**For Java Coders**

The return types are after the function, the ? mean "can be null", 

vals cant be reassigned, vars can, class ClassName(): URLClassLoader() {} means ClassName extends URLClassLoader

and PluginManager.kt is a static class (marked with object keyword at start)

Other than that, it is all java, and java classes and JVM

There is 3 usages of .filter {} and .map {} that might be a bit confusing but those work fine.

it can also load from local or over the internet. so thats kinda cool.
