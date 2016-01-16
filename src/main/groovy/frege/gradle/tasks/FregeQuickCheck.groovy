package frege.gradle.tasks
import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

class FregeQuickCheck extends DefaultTask {

    Boolean verbose = true
    Boolean listAvailable = false
    Boolean help = false
    Integer num = 100
    List<String> includePredicates
    List<String> excludePredicates
    String moduleName
    String moduleJar
    String moduleDir = "$project.buildDir/classes/test"
    List<String> classpathDirectories = ["$project.buildDir/classes/main", "$project.buildDir/classes/test"]
    List<String> allJvmArgs = []

    @TaskAction
    void runQuickCheck() {

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.tools.Quick")

        action.standardInput = System.in
        action.standardOutput = System.out
        action.errorOutput = System.err

        def f = project.files(classpathDirectories.collect { s -> new File(s) })
        action.setClasspath(project.files(project.configurations.compile).plus(project.files(project.configurations.testRuntime)).plus(f))

        def moduleSpec = moduleName ?: moduleJar ?: moduleDir

        def args = []
        if (help) {
            println """
FregeQuickCheck Help
--------------------
All attributes are optional,
currently used moduleDir  is '$moduleDir',
currently used moduleSpec is '$moduleSpec'.

Example attribute values:
fregeQuickCheck {
    help = true          // default: false
    listAvailable = true // default: false, will only list and not execute
    verbose = false      // default: true, needed to see the results
    num = 500            // default: 100
    includePredicates = ['myFirstPred', 'mySecondPred']
    excludePredicates = ['myFirstPred', 'mySecondPred']
    moduleName = 'my.cool.Module'                  // prio 1
    moduleJar  = 'path/to/my/module.jar'           // prio 2
    moduleDir  = "\$project.buildDir/classes/test" // prio 3, default
    classpathDirectories = ["\$project.buildDir/classes/main", "\$project.buildDir/classes/test"]
    allJvmArgs = ['-Xss4M']
}
"""
            println "Current Test Runtime is: "
            project.configurations.testRuntime.each { println it }
        }

        if (verbose) args << "-v"
        if (listAvailable) args << "-l"
        if (num) args << "-n" << num
        if (includePredicates) args << "-p" << includePredicates.join(',')
        if (excludePredicates) args << "-x" << excludePredicates.join(',')
        if (!allJvmArgs.isEmpty()) {
            action.setJvmArgs(allJvmArgs)
        }
        args << moduleSpec

        if (help) {
            println "Calling Frege QuickCheck with args: '${args.join(' ')}'"
            println "and JVM args: '${allJvmArgs.join(' ')}'"
        }
        action.args args
        action.execute()
    }

}