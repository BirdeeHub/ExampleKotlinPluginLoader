A plugin loader example program in kotlin. Use it if your program needs to load plugins!

This was a tiny part of another program but it came out so nicely I figured it could be useful to people before I finish the other project.

I then built a quick mockup test for it to upload it, in case someone finds it useful

The following is the link to the actual loader class:

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

build with 

```gradle build shadowJar```

go to outputDir on command line and run:

```java -jar ./examplepluginloader-all.jar```

it will give awarning message but will still work because I didn't want to spend 3 years on a gradle config for an example code snippet, and it doesnt happen in the application this was originally part of: 

this is the expected warning that will print before the output:

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

it will still work after the error, it prints test output from the plugin after the error as expected.

I was just too lazy to import the jar needed to make it not freak out about not being able to log without the proper dependencies

the original is a plugin system for a plugin for an original program that already had logging anyway

the expected output is the following, where the 4th line is a randomly generated UUID. It will be output after the error that is above

```
456
test
123
8cf4ce1f-6c47-4386-bacb-95a79813fae6
```