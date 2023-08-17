**A plugin loader example program in kotlin-jvm**

**THIS PLUGIN LOADER IS NOT YET VERY GOOD. IT CANNOT YET RELIABLY CLOSE THE PLUGINS IT CREATES, although I am trying**

It can load java and kotlin classes that implement MyPlugin that are defined inside .jar and .class files.

It can load them from file or directory, and you can specify package.class names to select if you want.

In theory, it SHOULD also close them individually or all at once, but this is proving to be quite difficult.

It also contains various useful reference functions

For optimum performance, do not load too many directories if they may have unrelated .jar or .class files in them.

It also can load bytecode from the internet. Use 1 or 2 to choose file or url. If leading argument is not an integer, it will behave as if in mode 1

For loading over http/s specify entire url to the file. It cannot "search the directory" over http

I then built a quick mockup test for it to upload it, in case someone finds it useful

I then kept returning to it when I got stuck on the bigger project and learned a ton about how class loaders work

I then learned that I should have made a much better test because it took me a long time to figure out my stuff didnt actually close, and now I need to learn that.

**The following is the link to the actual loader class:**

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

**Instructions:**

**To run the quick mockup:**

1st: go to root directory of the git repo.

Build first with: ```gradle build shadowJar``` (if desired)

to run:

stay in the same project root directory and run the following command:

```java -jar ./outputDir/examplepluginloader.jar```

default runs plugins from ./outputDir/plugins/, so if you go to outputDir on the command line you will need to run the following

```java -jar ./examplepluginloader.jar ./plugins/```

the expected output is the following:

for ```java -jar ./outputDir/examplepluginloader.jar```:
```
Testing...
Paths to load from:
./outputDir/plugins/
Tests:
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called (entering paths will set target to only plugin 1, removing this)
MyPluginImplementation1 getName() Test
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: 07a9c17e-59d1-45de-be7e-03ba62ebca48
MyPluginImplementation2 getName() Test (Filtering Demo)
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: b0999000-6451-4e41-a260-d01efcb38f4d
All UUIDs: [07a9c17e-59d1-45de-be7e-03ba62ebca48, b0999000-6451-4e41-a260-d01efcb38f4d]
type exit to attempt to close:
exit
Attempting to Unload: MyPluginImplementation1 getName() Test : 07a9c17e-59d1-45de-be7e-03ba62ebca48
All UUIDs: [b0999000-6451-4e41-a260-d01efcb38f4d]
Attempting to Unload: MyPluginImplementation2 getName() Test (Filtering Demo) : b0999000-6451-4e41-a260-d01efcb38f4d
All UUIDs: []
loaded 2 plugin(s)
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader.jar ./outputDir/plugins/```:
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
UUID: 6ebcf168-c775-42de-9613-88dcb77e66f9
All UUIDs: [6ebcf168-c775-42de-9613-88dcb77e66f9]
type exit to attempt to close:
exit
Attempting to Unload: MyPluginImplementation1 getName() Test : 6ebcf168-c775-42de-9613-88dcb77e66f9
All UUIDs: []
loaded 1 plugin(s)
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader.jar 2 https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar```
```
Testing...
Target classes:
exampleplugin.MyPluginImplementation1
Paths to load from:
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
Tests:
API call test when launchPlugin(api) is called
MyPluginImplementation1 getName() Test
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
UUID: b7c6f9ea-af76-49f7-8785-20389f5e910a
All UUIDs: [b7c6f9ea-af76-49f7-8785-20389f5e910a]
type exit to attempt to close:
exit
Attempting to Unload: MyPluginImplementation1 getName() Test : b7c6f9ea-af76-49f7-8785-20389f5e910a
All UUIDs: []
loaded 1 plugin(s)
Goodbye!
```