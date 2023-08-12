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
paths to load from:
./outputDir/plugins/
tests:
API call test when launchPlugin(api) is called (This gets filtered out if arguments are used for demonstration)
API call test when launchPlugin(api) is called
MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration)
UUID: 4ff003ba-2cbe-4cec-85d3-c3100587ebc3
MyPluginImplementation1 getName() Test
UUID: c39cb2ca-f01e-4cac-b91c-35a948199b23
All UUIDs: [4ff003ba-2cbe-4cec-85d3-c3100587ebc3, c39cb2ca-f01e-4cac-b91c-35a948199b23]
unloading: MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration) : 4ff003ba-2cbe-4cec-85d3-c3100587ebc3
All UUIDs: [c39cb2ca-f01e-4cac-b91c-35a948199b23]
unloading: MyPluginImplementation1 getName() Test : c39cb2ca-f01e-4cac-b91c-35a948199b23
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 316
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/plugins/ ./outputDir/plugins/exampleplugin.jar```:
```
Testing...
target classes:
exampleplugin.MyPluginImplementation1
paths to load from:
./outputDir/plugins/
./outputDir/plugins/exampleplugin.jar
tests:
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
MyPluginImplementation1 getName() Test
UUID: 88c8288e-75f2-44c1-85b2-1cc426ef94a8
MyPluginImplementation1 getName() Test
UUID: a8975448-e698-4cc6-ae04-75619e4f52bb
All UUIDs: [88c8288e-75f2-44c1-85b2-1cc426ef94a8, a8975448-e698-4cc6-ae04-75619e4f52bb]
unloading: MyPluginImplementation1 getName() Test : 88c8288e-75f2-44c1-85b2-1cc426ef94a8
All UUIDs: [a8975448-e698-4cc6-ae04-75619e4f52bb]
unloading: MyPluginImplementation1 getName() Test : a8975448-e698-4cc6-ae04-75619e4f52bb
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 318
Goodbye!
```