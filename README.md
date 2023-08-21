**A plugin loader example program in kotlin-jvm**

dont worry, theres no java. I copy pasted a game I made to use as an example plugin

it can also load from local or over the internet. so thats kinda cool.

the parent loader "MySystemLoader" is more or less just normal but with the api classpath included

The system loader exports the api and all dependencies. 

it runs the program which has the program and the plugin loading and management logic in its classpath, and inherits the api interfaces

the parent loader "MySystemLoader" also is to be the parent of PluginLoader so that it also inherits the api

PluginLoader contains wrapper functions for the normal ones that can be toggled off. It also contains another class loader.

It contains PluginClassLoader, which has all of its core functions overridden to use the wrapper functions in PluginLoader

create a new pluginLoader for a plugin, and then use the internal PluginClassLoader when you actually load the plugin

PluginClassLoader doesnt load anything for itself. PluginLoader does it, and can revoke access when close() is called.

If the plugin is loaded from pluginClassLoader, this means pluginLoader can completely prevent the plugin from loading classes.

The plugins can access the main program through the api object, such as any listeners and queries created as interfaces in the api. 

They will not be passed any other references to the main program.

The main program can define interfaces in the api object and pass it to the plugin for the plugin to query, 

the main program will retain a reference to the class loader 

and any objects passed as listeners, and close them when asked, PER PLUGIN

When you call shutdown, the plugin will not be able to load any new classes. 

But we cannot stop ones currently running. 

To fix this, you can implement an UnloadPluginHandler and register it with the 

registerShutdownSequence(unldHndlr: PluginUnloadHandler): Plugistration function in examplepluginloader.api.PluginManaging