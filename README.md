**A plugin loader example program in kotlin-jvm**

**THIS PLUGIN LOADER IS NOT YET VERY GOOD. IT CANNOT YET RELIABLY CLOSE THE PLUGINS IT CREATES, although I am trying**

it can however load from local or over the internet. so thats kinda cool.

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

The plan. Make PluginClassLoader and MySystemLoader as described in the comments within them. 

And then write a listener to tell the programs to close any isolated processes that would just keep spinning. 

And then maybe logging, but this is actually part of a plugin for another program that has plugins, and that program has logging so, maybe not.

the parent loader loads the program loader, which is more or less just normal but with the api classpath included

the parent loader "MySystemLoader" also loads the plugin loader. 

MySystemLoader contains extra wrapper functions for the normal ones that take a uuid and can be toggled off.

The plugin loader can only access its parent classloader MySystemLoader and nothing else.

Then, when close is called, it tells MySystemLoader to cut it off for its UUID.

The system loader can access the api and all dependencies. The plugins can access the parent loaders classpath, 

and the main program through the api object, such as any listeners and queries created as interfaces in the api. 

It will not be passed any other references to the main program.

The main program can define interfaces in the api object and pass it to the plugin for the plugin to query, 

and its implementations should ALSO cut off access if asked by the classloader.

the main program will retain a reference to the class loader 

and any objects passed as listeners, and close them when asked, PER PLUGIN

Im also going to need to create an api object factory because I need a copy of the api per plugin