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

for ```java -jar ./outputDir/examplepluginloader-all.jar ./outputDir/ ./outputDir/exampleplugin.jar```:
```
Testing...
./outputDir/
./outputDir/exampleplugin.jar
Plugin Api Call Test
Plugin Api Call Test
MyPluginImplementation Name Test
163dd770-777f-4641-9a07-e97db04083ce
MyPluginImplementation Name Test
a464f04c-c5c6-4979-bfa1-63d97dbebfbe
All UUIDs: [163dd770-777f-4641-9a07-e97db04083ce, a464f04c-c5c6-4979-bfa1-63d97dbebfbe]
unloading: MyPluginImplementation Name : 163dd770-777f-4641-9a07-e97db04083ce
All UUIDs: [a464f04c-c5c6-4979-bfa1-63d97dbebfbe]
unloading: MyPluginImplementation Name : a464f04c-c5c6-4979-bfa1-63d97dbebfbe
All UUIDs: []
Goodbye!
```