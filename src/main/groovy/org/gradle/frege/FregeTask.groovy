package org.gradle.frege

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction
import org.gradle.api.internal.file.FileResolver
import org.gradle.tooling.BuildException

class FregeTask extends DefaultTask {

    static String DEFAULT_CLASSES_SUBDIR = "classes/main"       // TODO: should this come from a convention?
    static String DEFAULT_SRC_DIR        = "src/main/frege"     // TODO: should this come from a source set?

    @Optional @Input
    boolean hints = false

    @Optional @Input
    boolean verbose = false

    @Optional @Input
    boolean inline = true

    @Optional @Input
    boolean make = true

    @Optional @Input
    boolean skipCompile = false

    @Optional @Input
    String extraArgs = ""

    @Optional @Input
    String allArgs = "" // this is an option to overrule all other settings

    @Optional @Input
    String module = ""

    @Optional @InputDirectory
    File sourceDir = new File(project.projectDir, DEFAULT_SRC_DIR)

    @Optional @OutputDirectory
    File outputDir = new File(project.buildDir, DEFAULT_CLASSES_SUBDIR)

    @TaskAction
    void executeCompile() {

        if (! sourceDir.exists() ) {
            throw new StopActionException("Source directory '${sourceDir.absolutePath}' does not exist. Cannot compile Frege code.")
        }
        if (! outputDir.exists() ) {
            logger.info "Creating output directory '${outputDir.absolutePath}'."
            outputDir.mkdirs()
        }

        // access extension configuration values as ${project.frege.key1}

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.compiler.Main")
        action.setClasspath(project.files(project.configurations.compile))

        def args = allArgs ? allArgs.split().toList() : assembleArguments()

        logger.info("Calling Frege compiler with args: '$args'")
        action.args(args)
        action.execute()
    }

    protected List assembleArguments() {
        List args = []
        if (hints)
            args << "-hints"
        if (inline)
            args << "-inline"
        if (make)
            args << "-make"
        if (verbose)
            args << "-v"
        if (skipCompile)
            args << "-j"

        args << "-sp"
        args << sourceDir.absolutePath

        args << "-d"
        args << outputDir

        if (!module && !extraArgs) {
            logger.info "no module and no extra args given: compiling all of the sourceDir"
            args << sourceDir.absolutePath
        } else if (module) {
            logger.info "compiling module '$module'"
            args << module
        } else {
            args = args + extraArgs.split().toList()
        }
        args
    }
}