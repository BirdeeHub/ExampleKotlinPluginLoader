**A plugin loader example program in kotlin-jvm**

dont worry, theres no java. I copy pasted a game I made to use as an example plugin.

it can also load from local or over the internet. so thats kinda cool.

I learned to use a profiler and found out that **IT STILL DOES NOT UNLOAD APPARENTLY** 

(yes even if theres no minesweeper in the plugins jar and only the 2 super basic ones.)

**HELP** Im pretty stuck at this point. I dont see any more references to remove....

Start at exampleParentLoader, it sets up 2 classloaders, 

1 that is overall parent, 1 that is for main program with overall parent as parent

in examplepluginloader is the main logic. It has the classloader for the plugins, 

which can cut off loading access and has the overall parent loader as its parent.

PluginManager manages it, api allows plugins to exist and also get shutdown handler event.

This is part of another project and AAAAAAAAAAHHHHHHHHHHH..............

I just want to finally get to write the main features at this point..... 

But i want a working base....