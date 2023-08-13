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
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: 58eb00c6-8ca5-4b75-bfe1-31a3b6ee46a2
MyPluginImplementation1 getName() Test
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: f3d28a7a-b4f8-42e7-ba83-88959b86121c
All UUIDs: [58eb00c6-8ca5-4b75-bfe1-31a3b6ee46a2, f3d28a7a-b4f8-42e7-ba83-88959b86121c]
unloading: MyPluginImplementation2 getName() Test (This gets filtered out if arguments are used for demonstration) : 58eb00c6-8ca5-4b75-bfe1-31a3b6ee46a2
All UUIDs: [f3d28a7a-b4f8-42e7-ba83-88959b86121c]
unloading: MyPluginImplementation1 getName() Test : f3d28a7a-b4f8-42e7-ba83-88959b86121c
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 313
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
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: 95d6bc35-e65a-49c2-917d-dc88cf45dd4a
MyPluginImplementation1 getName() Test
C:\Users\robin\Desktop\temp\examplepluginloader\.\outputDir\plugins\exampleplugin.jar
UUID: 271e0928-8690-41c1-8b3f-583445dc2d3f
All UUIDs: [95d6bc35-e65a-49c2-917d-dc88cf45dd4a, 271e0928-8690-41c1-8b3f-583445dc2d3f]
unloading: MyPluginImplementation1 getName() Test : 95d6bc35-e65a-49c2-917d-dc88cf45dd4a
All UUIDs: [271e0928-8690-41c1-8b3f-583445dc2d3f]
unloading: MyPluginImplementation1 getName() Test : 271e0928-8690-41c1-8b3f-583445dc2d3f
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 314
Goodbye!
```