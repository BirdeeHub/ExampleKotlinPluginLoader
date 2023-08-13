**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

You will need a copy of JDK 17. The kotlin runtime is in the jar.

It can load any number of classes that implement MyPlugin that are defined inside the first package or (no package) of .jar and .class files.

It can load them from file or directory and they can be in java or kotlin, and you can specify package.class names if you want.

It can also close them individually or all at once, and contains various useful reference functions

For optimum performance, do not load too many directories if they may have unrelated .jar or .class files in them.

It was a tiny part of another program but it came out so nicely I figured it could be useful to people before I finish the other project.

I then built a quick mockup test for it to upload it, in case someone finds it useful

**The following is the link to the actual loader class:**

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

**Instructions:**

if you wish to use, you probably only need the thing i linked.

**To run the quick mockup:**

1st: go to root directory of the git repo.

Build first with: ```gradle clean build shadowJar``` (if desired)

to run:

stay in the same project root directory and run the following command:

```java -jar ./outputDir/examplepluginloader-all.jar```

default runs plugins from ./outputDir, so if you go to outputDir on the command line you will need to run the following

```java -jar ./examplepluginloader-all.jar ./plugins/```

the expected output is the following:

for ```java -jar ./outputDir/examplepluginloader-all.jar```:
```
Testing...
Paths to load from:
./outputDir/plugins/
Tests:
API call test when launchPlugin(api) is called (This gets filtered out if arguments are used for demonstration)
API call test when launchPlugin(api) is called
MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration)
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: 5f82312f-d681-4d69-a834-dafd20338c1d
MyPluginImplementation1 getName() Test
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: 3690bf20-cef1-422f-938f-38364df870c8
All UUIDs: [5f82312f-d681-4d69-a834-dafd20338c1d, 3690bf20-cef1-422f-938f-38364df870c8]
Unloading: MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration) : 5f82312f-d681-4d69-a834-dafd20338c1d
All UUIDs: [3690bf20-cef1-422f-938f-38364df870c8]
Unloading: MyPluginImplementation1 getName() Test : 3690bf20-cef1-422f-938f-38364df870c8
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 353
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/plugins/ ./outputDir/plugins/exampleplugin.jar```:
```
Testing...
Target classes:
exampleplugin.MyPluginImplementation1
Paths to load from:
./outputDir/plugins/
./outputDir/plugins/exampleplugin.jar
Tests:
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
MyPluginImplementation1 getName() Test
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: 7e931beb-7a41-4e64-bfce-53c42d3abf35
MyPluginImplementation1 getName() Test
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: aae80835-3ed9-43ff-bb9a-a9c0ab243baa
All UUIDs: [7e931beb-7a41-4e64-bfce-53c42d3abf35, aae80835-3ed9-43ff-bb9a-a9c0ab243baa]
Unloading: MyPluginImplementation1 getName() Test : 7e931beb-7a41-4e64-bfce-53c42d3abf35
All UUIDs: [aae80835-3ed9-43ff-bb9a-a9c0ab243baa]
Unloading: MyPluginImplementation1 getName() Test : aae80835-3ed9-43ff-bb9a-a9c0ab243baa
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 322
Goodbye!
```