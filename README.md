# Frege Gradle Plugin

Compile frege code with gradle.

## Installation

```bash
git clone https://github.com/tricktron/frege-gradle-plugin.git
./gradlew publishToMavenLocal
```

## How to Use
1. Specify the frege compiler release and version in your `build.gradle`:

```groovy
frege {
    version = '3.25.84'
    release = '3.25alpha'
}
```

See the [frege releases](https://github.com/Frege/frege/releases) for all available versions.

2. Run the newly added `compileFrege` task. All your `*.fr` files get compiled to `build/classes/frege`.

## How to Contribute
Try to add another task, e.g. `fregeDoc` to the [FregePluginFunctionalTest.java](src/functionalTest/java/ch/fhnw/thga/gradleplugins/FregePluginFunctionalTest.java) file and try to make the test pass.