A plugin loader example program in kotlin.

This exists because its a fairly standalone example created while making another program

[examplepluginloader.PluggerXP.PluginLoader](examplepluginloader/src/main/kotlin/examplepluginloader/PluggerXP/PluginLoader.kt)

build with 

```gradle build shadowJar```

copy:

```examplepluginloader/build/libs/examplepluginloader-all.jar```

and

```exampleplugin/build/libs/exampleplugin.jar```

into the same directory as one another, then run: 

```java -jar ./examplepluginloader-all.jar```

it will give an error but still work because I didn't want to spend 3 years on a gradle config for an example code snippet, 

and it doesnt give me an error in the application this was originally part of: 

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

it will still work after the error, it prints test output from the plugin after the error as expected.

I was just too lazy to import the jar needed to make it not freak out about not being able to log without the proper dependencies