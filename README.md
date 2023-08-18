**A plugin loader example program in kotlin-jvm**

**THIS PLUGIN LOADER IS NOT YET VERY GOOD. IT CANNOT YET RELIABLY CLOSE THE PLUGINS IT CREATES, although I am trying**

it can however load from local or over the internet. so thats kinda cool.

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

The plan. Make PluginClassLoader and MySystemLoader as described in the comments within them. 

And then write a listener to tell the programs to close any isolated processes that would just keep spinning. 

And then maybe logging, but this is actually part of a plugin for another program that has plugins, and that program has logging so, maybe not.