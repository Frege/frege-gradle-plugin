package frege.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

class DocTask extends DefaultTask {

    /* Usage: java -jar fregec.jar frege.tools.Doc [-v] [-d opt] [-x mod,...] modules ...
     * -v              print a message for each processed module
     * -d docdir       specify root directory for documentation
     *                 Documentation for module x.y.Z will be writen to
     *                 $docdir/x/y/Z.html
     * -cp classpath   class path for doc tool
     * -x mod1[,mod2]  exclude modules whose name starts with 'mod1' or 'mod2'
     *
     * Modules can be specified in three ways:
     *  my.nice.Modul   by name, the Java class for this module must be on the class path
     *  directory/      all modules that could be loaded if the given directory was on the class path, except exxcluded ones
     *  path.jar        all modules in the specified JAR file, except excluded ones
     *
     * Example: document base frege distribution without compiler modules
     *      java -cp fregec.jar frege.tools.Doc -d doc -x frege.compiler fregec.jar
     *
     */

    static String DEFAULT_SRC_DIR     = "src/main/frege"     // TODO: should this come from a source set?
    static String DEFAULT_DOCS_SUBDIR = "docs/frege"       // TODO: should this come from a convention?

    Boolean help = false

    @Optional
    @InputDirectory
    File sourceDir = new File(project.projectDir, DEFAULT_SRC_DIR).exists() ? new File(project.projectDir, DEFAULT_SRC_DIR) : null

    @Optional
    @OutputDirectory
    File targetDir = new File(project.buildDir, DEFAULT_DOCS_SUBDIR)

    @Input
    String module = "$project.buildDir/classes/main" // module name or directory or class path. Default is all production modules

    @Input @Optional
    String exclude = null

    @Input @Optional
    Boolean verbose = null

    @TaskAction
    void fregedoc() {

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.tools.Doc")
        action.workingDir = sourceDir ?: project.projectDir
        action.standardInput = System.in
        action.standardOutput = System.out
        action.errorOutput = System.err
        action.setClasspath(project.files(project.configurations.compile) + project.files("$project.buildDir/classes/main"))

        def args = []
        if (help) {
            args << "-h"
        } else {
            if (verbose) args << '-v'
            args << '-d' << targetDir.absolutePath
            if (exclude) args << '-x' << exclude
            args << module
        }

        logger.info("Calling Frege Doc with args: '$args'")
        action.args args
        action.execute()
    }

}

