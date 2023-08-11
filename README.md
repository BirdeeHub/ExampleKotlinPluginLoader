**A plugin loader example program in kotlin-jvm**

Use it if your program needs to load plugins!

This was a tiny part of another program but it came out so nicely I figured it could be useful to people before I finish the other project.

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

```
Testing...
./outputDir/
1...
2...
b383ebcb-2334-4213-b945-3c34d280483d
[b383ebcb-2334-4213-b945-3c34d280483d]
Goodbye!
```

The 4th line is a randomly generated UUID for referencing the test plugin.