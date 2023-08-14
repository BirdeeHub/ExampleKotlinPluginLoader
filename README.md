**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

You will need a copy of JDK 17. The kotlin runtime is in the jar.

It can load any number of classes that implement MyPlugin that are defined inside the first package or (no package) of .jar and .class files.

It can load them from file or directory and they can be in java or kotlin, and you can specify package.class names if you want.

It can also close them individually or all at once, and contains various useful reference functions

For optimum performance, do not load too many directories if they may have unrelated .jar or .class files in them.

It now also can load classes from the internet. Use 1 or 2 to choose file or url. If leading argument is not an integer, it will behave as if in mode 1

For loading over http/s specify entire url to jar or class file. 

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
API call test when launchPlugin(api) is called (entering paths will set target to only plugin 1, removing this)
API call test when launchPlugin(api) is called
MyPluginImplementation2 getName() Test (Filtering Demo)
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: 50a8f6fe-c620-4cf0-a51f-0241bf400f2e
MyPluginImplementation1 getName() Test
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: 42e16365-babb-4da3-a362-1264c07bed53
All UUIDs: [50a8f6fe-c620-4cf0-a51f-0241bf400f2e, 42e16365-babb-4da3-a362-1264c07bed53]
Unloading: MyPluginImplementation2 getName() Test (Filtering Demo) : 50a8f6fe-c620-4cf0-a51f-0241bf400f2e
All UUIDs: [42e16365-babb-4da3-a362-1264c07bed53]
Unloading: MyPluginImplementation1 getName() Test : 42e16365-babb-4da3-a362-1264c07bed53
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 295
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/plugins/```:
```
Testing...
Target classes:
exampleplugin.MyPluginImplementation1
Paths to load from:
./outputDir/plugins/
Tests:
API call test when launchPlugin(api) is called
MyPluginImplementation1 getName() Test
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: fb0a9b90-7cf4-48e5-8f52-75eefad5f155
All UUIDs: [fb0a9b90-7cf4-48e5-8f52-75eefad5f155]
Unloading: MyPluginImplementation1 getName() Test : fb0a9b90-7cf4-48e5-8f52-75eefad5f155
All UUIDs: []
loaded and unloaded 1 plugin(s)
duration in milliseconds: 309
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar 2```
```
Testing...
Paths to load from:
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
Tests:
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called (This gets filtered out if arguments are used for demonstration)
MyPluginImplementation1 getName() Test
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
UUID: f2ada57e-003f-4cf8-88e5-15e3a0f26f59
MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration)
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
UUID: bec4d115-21f0-4bc4-b495-b5752ac5acab
All UUIDs: [f2ada57e-003f-4cf8-88e5-15e3a0f26f59, bec4d115-21f0-4bc4-b495-b5752ac5acab]
Unloading: MyPluginImplementation1 getName() Test : f2ada57e-003f-4cf8-88e5-15e3a0f26f59
All UUIDs: [bec4d115-21f0-4bc4-b495-b5752ac5acab]
Unloading: MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration) : bec4d115-21f0-4bc4-b495-b5752ac5acab
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 924
Goodbye!
```