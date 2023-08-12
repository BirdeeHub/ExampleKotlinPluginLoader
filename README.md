**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

You will need a copy of JDK 17. The kotlin runtime is in the jar.

It can load any number of classes that implement MyPlugin that are defined inside .jar and .class files.

It can load them from file or directory and they can be in java or kotlin, and you can specify package names if you want.

It can also close them individually or all at once, and contains various useful reference functions

For optimum performance, do not load too many directories if they may have unrelated .jar files in them.

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

```java -jar ./examplepluginloader-all.jar ./plugins/```

the expected output is the following:

for ```java -jar ./outputDir/examplepluginloader-all.jar```:
```
Testing...
./outputDir/plugins/
API call test when launchPlugin(api) is called
MyPluginImplementation getName() Test
UUID: 8424ef3f-8cba-43aa-b3b5-e0eb348cb62f
All UUIDs: [8424ef3f-8cba-43aa-b3b5-e0eb348cb62f]
unloading: MyPluginImplementation getName() Test : 8424ef3f-8cba-43aa-b3b5-e0eb348cb62f
All UUIDs: []
loaded and unloaded 1 plugin(s)
duration in milliseconds: 343
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/plugins/ ./outputDir/plugins/exampleplugin.jar```:
```
Testing...
./outputDir/plugins/
./outputDir/plugins/exampleplugin.jar
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
MyPluginImplementation getName() Test
UUID: bd6f17cd-dc3a-4e0a-a61f-dd82d5745186
MyPluginImplementation getName() Test
UUID: d8b40712-e175-48be-bd77-1fad8ed2d686
All UUIDs: [bd6f17cd-dc3a-4e0a-a61f-dd82d5745186, d8b40712-e175-48be-bd77-1fad8ed2d686]
unloading: MyPluginImplementation getName() Test : bd6f17cd-dc3a-4e0a-a61f-dd82d5745186
All UUIDs: [d8b40712-e175-48be-bd77-1fad8ed2d686]
unloading: MyPluginImplementation getName() Test : d8b40712-e175-48be-bd77-1fad8ed2d686
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 320
Goodbye!
```