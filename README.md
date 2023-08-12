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

for ```java -jar ./outputDir/examplepluginloader-all.jar```:
```
Testing...
./outputDir/
Plugin Api Call Test
MyPluginImplementation Name
f498dff0-8e8e-49e8-89ad-445a2d02f614
All UUIDs: [f498dff0-8e8e-49e8-89ad-445a2d02f614]
unloading: MyPluginImplementation Name : f498dff0-8e8e-49e8-89ad-445a2d02f614
All UUIDs: []
Goodbye!
```

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/ ./outputDir/exampleplugin.jar C:\Users\robin\Desktop\temp\examplepluginloader\outputDir\exampleplugin.jar```:
```
Testing...
./outputDir/
./outputDir/exampleplugin.jar
C:\\Users\\robin\\Desktop\\temp\\examplepluginloader\\outputDir\\exampleplugin.jar
Plugin Api Call Test
Plugin Api Call Test
Plugin Api Call Test
MyPluginImplementation Name
844e0d13-1205-4761-90a4-16e1994341dd
MyPluginImplementation Name
6ac0fa05-e93d-40d0-8690-f782a1491e1e
MyPluginImplementation Name
3d92565d-48bd-4a82-8e1a-2c937ec0bb77
All UUIDs: [844e0d13-1205-4761-90a4-16e1994341dd, 6ac0fa05-e93d-40d0-8690-f782a1491e1e, 3d92565d-48bd-4a82-8e1a-2c937ec0bb77]
unloading: MyPluginImplementation Name : 844e0d13-1205-4761-90a4-16e1994341dd
All UUIDs: [6ac0fa05-e93d-40d0-8690-f782a1491e1e, 3d92565d-48bd-4a82-8e1a-2c937ec0bb77]
unloading: MyPluginImplementation Name : 6ac0fa05-e93d-40d0-8690-f782a1491e1e
All UUIDs: [3d92565d-48bd-4a82-8e1a-2c937ec0bb77]
unloading: MyPluginImplementation Name : 3d92565d-48bd-4a82-8e1a-2c937ec0bb77
All UUIDs: []
Goodbye!
```