**A plugin loader example program in kotlin-jvm**

**This plugin loader does not yet allow plugins to define a shutdown hook to shut down currently running threads**

**but it will at least prevent them from accessing anything else when close is called**

it can also load from local or over the internet. so thats kinda cool.

the parent loader "MySystemLoader" loads the program loader, which is more or less just normal but with the api classpath included

the parent loader "MySystemLoader" also is to be the parent of PluginLoader. 

PluginLoader contains wrapper functions for the normal ones that can be toggled off.

It contains PluginClassLoader, which has all of its core functions overridden to use the wrapper functions in PluginLoader

create a new pluginLoader for a plugin, and then use the internal PluginClassLoader when you actually load the plugin

PluginClassLoader doesnt load anything for itself. PluginLoader does it, and can revoke access when close() is called.

If the plugin is loaded from pluginClassLoader, this means pluginLoader can completely prevent the plugin from loading classes.

The system loader can access the api and exports all dependencies. 

The plugins can access the main program through the api object, such as any listeners and queries created as interfaces in the api. 

They will not be passed any other references to the main program.

The main program can define interfaces in the api object and pass it to the plugin for the plugin to query, 

and its implementations should ALSO cut off access if asked by the classloader.

the main program will retain a reference to the class loader 

and any objects passed as listeners, and close them when asked, PER PLUGIN

Im also going to need to create an api object factory because I need a copy of the api per plugin