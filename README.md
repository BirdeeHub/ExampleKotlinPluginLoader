**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

You will need a copy of JDK 17. The kotlin runtime is in the jar.

It can load any number of classes that implement MyPlugin that are defined inside .jar and .class files.

It can load them from file or directory and they can be in java or kotlin, and you can specify package names if you want.

It can also close them individually or all at once, and contains various useful reference functions

For optimum performance, do not load too many directories if they may have unrelated .jar files in them (such as in the outputDir of this repo).

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

for ```java -jar ./outputDir/examplepluginloader-all.jar```:
```
Testing...
./outputDir/
API call test when launchPlugin(api) is called
MyPluginImplementation getName() Test
UUID: c94b0a9b-5a96-4488-98ea-b6fab5bb223f
All UUIDs: [c94b0a9b-5a96-4488-98ea-b6fab5bb223f]
unloading: MyPluginImplementation getName() Test : c94b0a9b-5a96-4488-98ea-b6fab5bb223f
All UUIDs: []
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/ ./outputDir/exampleplugin.jar```:
```
Testing...
./outputDir/
./outputDir/exampleplugin.jar
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
MyPluginImplementation getName() Test
UUID: 68bd226d-0e46-46b8-8118-bded14c825e6
MyPluginImplementation getName() Test
UUID: 64e49eb9-e6d0-419e-8127-f8c67d812211
All UUIDs: [68bd226d-0e46-46b8-8118-bded14c825e6, 64e49eb9-e6d0-419e-8127-f8c67d812211]
unloading: MyPluginImplementation getName() Test : 68bd226d-0e46-46b8-8118-bded14c825e6
All UUIDs: [64e49eb9-e6d0-419e-8127-f8c67d812211]
unloading: MyPluginImplementation getName() Test : 64e49eb9-e6d0-419e-8127-f8c67d812211
All UUIDs: []
Goodbye!
```