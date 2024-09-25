# IDEA-359687

This repo contains code to reproduce [IDEA-359687](https://youtrack.jetbrains.com/issue/IDEA-359687/ClassNotFoundException-when-debugging-OSGi-application-with-IntelliJ-Idea-2024.2.1).
Since the bug is OSGi related there is a bit of code to even get the minimal example up and running.
The relevant code for  the bug is in [UiController](module/src/main/kotlin/io/github/hanseter/module/a/UiController.kt), the code in [startup](startup) and [app](app) is only there to start OSGi.

## How to build

Simply use maven to build the code.

```shell
mvn clean install
```

## How to run

An IntelliJ configuration to run the code is provided in this repo.
The configuration will automatically attach a debugger to the started program.
Alternatively you can run the software by executing
```shell
mvn exec:exec
```

## How to reproduce
After building and running  simply click the button with text "click me".
Observer the output in the console in IntelliJ.
You should now see the exception.