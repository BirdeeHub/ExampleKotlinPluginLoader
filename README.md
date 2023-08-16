**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

You will need a copy of JDK 17. The kotlin runtime is in the jar, but you will need that and 1 dependency if you wish to compile.

It can load java and kotlin classes that implement MyPlugin that are defined inside .jar and .class files.

It can load them from file or directory, and you can specify package.class names to select if you want.

It can also close them individually or all at once, and contains various useful reference functions

For optimum performance, do not load too many directories if they may have unrelated .jar or .class files in them.

It now also can load bytecode from the internet. Use 1 or 2 to choose file or url. If leading argument is not an integer, it will behave as if in mode 1

For loading over http/s specify entire url to the file. It cannot "search the directory" over http

This was a tiny part of another program but it came out so nicely I figured it could be useful to people before I finish the other project.

I then built a quick mockup test for it to upload it, in case someone finds it useful

I then kept returning to it when I got stuck on the bigger project and learned a ton about how class loaders work

**The following is the link to the actual loader class:**

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

**Instructions:**

If you wish to use, you probably only need the thing I linked. Replace all on the word "My" so that it matches YourPlugin and YourAPI after

**To run the quick mockup:**

1st: go to root directory of the git repo.

Build first with: ```gradle build shadowJar``` (if desired)

to run:

stay in the same project root directory and run the following command:

```java -jar ./outputDir/examplepluginloader-all.jar```

default runs plugins from ./outputDir/plugins/, so if you go to outputDir on the command line you will need to run the following

```java -jar ./examplepluginloader-all.jar ./plugins/```

the expected output is the following:

for ```java -jar ./outputDir/examplepluginloader-all.jar```:
```
Testing...
Paths to load from:
./outputDir/plugins/
Tests:
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called (entering paths will set target to only plugin 1, removing this)
MyPluginImplementation1 getName() Test
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: fbf33b50-50af-49db-b00a-41a72c97a272
MyPluginImplementation2 getName() Test (Filtering Demo)
file:/C:/Users/robin/Desktop/temp/examplepluginloader/./outputDir/plugins/exampleplugin.jar
UUID: fd067648-f31d-4d79-91ba-8f4d046ed6f3
All UUIDs: [fbf33b50-50af-49db-b00a-41a72c97a272, fd067648-f31d-4d79-91ba-8f4d046ed6f3]
Unloading: MyPluginImplementation1 getName() Test : fbf33b50-50af-49db-b00a-41a72c97a272
All UUIDs: [fd067648-f31d-4d79-91ba-8f4d046ed6f3]
Unloading: MyPluginImplementation2 getName() Test (Filtering Demo) : fd067648-f31d-4d79-91ba-8f4d046ed6f3
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 152
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
UUID: ce135b8c-8fb8-4abd-8be0-f988799aba88
All UUIDs: [ce135b8c-8fb8-4abd-8be0-f988799aba88]
Unloading: MyPluginImplementation1 getName() Test : ce135b8c-8fb8-4abd-8be0-f988799aba88
All UUIDs: []
loaded and unloaded 1 plugin(s)
duration in milliseconds: 154
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar 2```
```
Testing...
Paths to load from:
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
Tests:
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called (entering paths will set target to only plugin 1, removing this)
MyPluginImplementation1 getName() Test
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
UUID: 5cd9a690-cc00-4924-b64c-5e07931baafa
MyPluginImplementation2 getName() Test (Filtering Demo)
https://github.com/BirdeeHub/ExampleKotlinPluginLoader/raw/main/outputDir/plugins/exampleplugin.jar
UUID: b3cb5c9c-5001-4657-9c08-8f6d048552e7
All UUIDs: [5cd9a690-cc00-4924-b64c-5e07931baafa, b3cb5c9c-5001-4657-9c08-8f6d048552e7]
Unloading: MyPluginImplementation1 getName() Test : 5cd9a690-cc00-4924-b64c-5e07931baafa
All UUIDs: [b3cb5c9c-5001-4657-9c08-8f6d048552e7]
Unloading: MyPluginImplementation2 getName() Test (Filtering Demo) : b3cb5c9c-5001-4657-9c08-8f6d048552e7
All UUIDs: []
loaded and unloaded 2 plugin(s)
duration in milliseconds: 779
Goodbye!
```