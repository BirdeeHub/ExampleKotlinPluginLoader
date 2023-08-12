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
./outputDir/plugins/
API call test when launchPlugin(api) is called
MyPluginImplementation1 getName() Test
UUID: 4d765137-2e26-4a6d-a46b-88f6501dafb0
All UUIDs: [4d765137-2e26-4a6d-a46b-88f6501dafb0]
unloading: MyPluginImplementation1 getName() Test : 4d765137-2e26-4a6d-a46b-88f6501dafb0
All UUIDs: []
loaded and unloaded 1 plugin(s)
duration in milliseconds: 303
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/plugins/ ./outputDir/plugins/exampleplugin.jar```:
```
Testing...
./outputDir/plugins/
./outputDir/plugins/exampleplugin.jar
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
API call test when launchPlugin(api) is called
MyPluginImplementation2 getName() Test (This gets filtered out if no arguments are used)
UUID: 82fe71ee-b95b-4021-b1b1-02b2c06555ca
MyPluginImplementation1 getName() Test
UUID: c708bad5-544e-414f-967f-30201cabb1e9
MyPluginImplementation2 getName() Test (This gets filtered out if no arguments are used)
UUID: 282f2ccf-39a6-49d1-917b-4755f21c2bb4
MyPluginImplementation1 getName() Test
UUID: 5b87ffde-beb7-489a-aea5-7d18b5e1cd94
All UUIDs: [82fe71ee-b95b-4021-b1b1-02b2c06555ca, c708bad5-544e-414f-967f-30201cabb1e9, 282f2ccf-39a6-49d1-917b-4755f21c2bb4, 5b87ffde-beb7-489a-aea5-7d18b5e1cd94]
unloading: MyPluginImplementation2 getName() Test (This gets filtered out if no arguments are used) : 82fe71ee-b95b-4021-b1b1-02b2c06555ca
All UUIDs: [c708bad5-544e-414f-967f-30201cabb1e9, 282f2ccf-39a6-49d1-917b-4755f21c2bb4, 5b87ffde-beb7-489a-aea5-7d18b5e1cd94]
unloading: MyPluginImplementation1 getName() Test : c708bad5-544e-414f-967f-30201cabb1e9
All UUIDs: [282f2ccf-39a6-49d1-917b-4755f21c2bb4, 5b87ffde-beb7-489a-aea5-7d18b5e1cd94]
unloading: MyPluginImplementation2 getName() Test (This gets filtered out if no arguments are used) : 282f2ccf-39a6-49d1-917b-4755f21c2bb4
All UUIDs: [5b87ffde-beb7-489a-aea5-7d18b5e1cd94]
unloading: MyPluginImplementation1 getName() Test : 5b87ffde-beb7-489a-aea5-7d18b5e1cd94
All UUIDs: []
loaded and unloaded 4 plugin(s)
duration in milliseconds: 305
Goodbye!
```