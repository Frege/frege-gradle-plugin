# Frege Gradle Plugin

![build](https://github.com/tricktron/frege-gradle-plugin/actions/workflows/build.yml/badge.svg)

Simplifies setting up your Frege project.

## Installation

```bash
git clone https://github.com/tricktron/frege-gradle-plugin.git
./gradlew publishToMavenLocal
```

## How to Use
1. Specify the frege compiler release, version and main module in your `build.gradle`:

```groovy
frege {
    version = '3.25.84'
    release = '3.25alpha'
    mainModule = 'my.mod.Name'
}
```

See the [Frege Releases](https://github.com/Frege/frege/releases) for all available versions.

Optional configuration parameters inside `build.gradle`:
- compilerDownloadDir: defaults to `<projectRoot>/lib`
- mainSourceDir: defaults to `<projectRoot>/src/main/frege`
- outputDir: defaults to `<projectRoot>/build/classes/main/frege`
- compilerFlags: defaults to `['-O', '-make']`




### Added Tasks

- **setupFrege**: Downloads the specified version of the Frege compiler.
- **compileFrege**: All your `*.fr` files in `mainSourceDir` get compiled to `outputDir`.
- **runFrege**: Runs the Frege module specified by `mainModule`. Alternatively you can also pass the main module by command line, e.g: `gradle runFrege --mainModule=my.mod.Name`.


## How to Contribute
Try to add another task, e.g. `fregeDoc` to the [FregePluginFunctionalTest.java](src/functionalTest/java/ch/fhnw/thga/gradleplugins/FregePluginFunctionalTest.java) file and try to make the test pass.