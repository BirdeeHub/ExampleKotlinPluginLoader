**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

You will need a copy of JDK 17. The kotlin runtime is in the jar.

It can load any number of classes that implement MyPlugin that are defined inside .jar and .class files.

It can load them from file or directory and they can be in java or kotlin

It can also close them individually or all at once, and contains various useful reference functions

It was a tiny part of another program but it came out so nicely I figured it could be useful to people before I finish the other project.

I then built a quick mockup test for it to upload it, in case someone finds it useful

The following is the link to the actual loader class:

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

Instructions:

1st: go to root directory of the git repo.

Build first with: ```gradle clean build shadowJar``` (if desired)

to run:

stay in the same project root directory and run the following command:

```java -jar ./outputDir/examplepluginloader-all.jar```

default runs plugins from ./outputDir, so if you go to outputDir on the command line you will need to run the following

```java -jar ./examplepluginloader-all.jar ./```

the expected output is the following:

```
Testing...
./outputDir/
Test 1...
MyPluginImplementation loaded
8dac5dc3-f486-4f1d-9263-05581c0104e9
[8dac5dc3-f486-4f1d-9263-05581c0104e9]
Goodbye!
```

./outputDir/ is where it is currently looking for plugins at.

Test 1... is printed by the plugin when the plugin's launchPlugin(api: MyAPI) is called. It calls api.test(), which calls test() defined in MyAPIobj, passed as MyAPI

MyPluginImplementation loaded is printed when we call plugin.test()

8dac5dc3-f486-4f1d-9263-05581c0104e9 is a randomly generated UUID for referencing the test plugin.

\[8dac5dc3-f486-4f1d-9263-05581c0104e9\] is the list of all UUIDs for all plugins loaded.