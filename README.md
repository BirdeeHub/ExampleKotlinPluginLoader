A plugin loader example program in kotlin. Use it if your program needs to load plugins!

This was a tiny part of another program but it came out so nicely I figured it could be useful to people before I finish the other project.

I then built a quick mockup test for it to upload it, in case someone finds it useful

The following is the link to the actual loader class:

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

Instructions:

1st: go to root directory of the git repo.

then, build with:

```gradle build shadowJar```

Building first is optional because the jar files are already in outputDir on the repo

to run, stay in the same project root directory and run the following command:

```java -jar ./outputDir/examplepluginloader-all.jar```

default runs plugins from outputDir, so if you go to outputDir on the command line you will need to run the following

```java -jar ./outputDir/examplepluginloader-all.jar ./```

the expected output is the following, where the 4th line is a randomly generated UUID.

```
Testing...
1...
2...
3ca92eb9-2db6-4631-85ba-8bacff3f0899
Goodbye!
```
