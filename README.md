**A plugin loader example program in kotlin-jvm**

It should be pretty readable for Java coders. 

The return types are after the function, the question marks mean "can be null"

and PluginManager.kt is a static class (marked with object keyword at start)

Other than that, it is all java, and java classes and JVM

The 3 weird filtering functions do work correctly, which are probably the only other part that isnt just java.

it can also load from local or over the internet. so thats kinda cool.

gradle is used for build, so just ```gradle build shadowJar```

Default plugin directory when you run it assumes you are at project root but you can specify.

```java -jar ExampleOut/entryPoint.jar 1 <optional path to plugins>```

----------------------------------------------------------------------------------------------

I learned to use a profiler and found out that **IT STILL DOES NOT UNLOAD APPARENTLY** 

(yes even if theres no minesweeper in the plugins jar and only the 2 super basic ones.)

**HELP** Im pretty stuck at this point. I dont see any more references to remove....

Basically, I know that you cannot be holding any remaining references to the class.

But I thought I removed them all....

Is the way I wrote my custom class loader in PluginLoader preventing it from being garbage collected somehow?

or is it that im doing the managing from a singleton and not a classloader somehow?

or is it somewhere in the api implementation I pass to the plugin?

The parts of note are at the following locations:

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/blob/main/entryPoint/src/main/kotlin/examplepluginloader/main.kt

https://github.com/BirdeeHub/ExampleKotlinPluginLoader/tree/main/examplepluginloader/src/main/kotlin/examplepluginloader

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