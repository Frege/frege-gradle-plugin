package org.gradle.frege

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction
import org.gradle.api.internal.file.FileResolver

class FregeTask extends DefaultTask {

    private static final FREGE_FILE_EXTENSION_PATTERN = ~/.*\.fr?$/

    @Input
    boolean hints

    @Input
    boolean verbose

    @Input
    boolean inline = true

    @Input
    boolean make = true

    @Input
    boolean skipCompile

    @Input
    boolean includeStale

    // TODO: Find default
    @OutputDirectory
    File outputDir = new File("build/classes/main")

    @TaskAction
    void executeCompile() {
        println "Compiling Frege to " + outputDir
        // access extension configuration values as ${project.frege.key1}

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.compiler.Main")
        action.setClasspath(project.files(project.configurations.compile))

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

        args << "-d"
        args << outputDir


        eachFileRecurse(new File("src/main/frege")) { File file ->
            if (file.name =~ FREGE_FILE_EXTENSION_PATTERN) {
                args << file
            }

        }

        println("FregeTask args: $args")
        action.args(args)

        action.execute()
    }

    private static void eachFileRecurse(File dir, Closure fileProcessor) {
        dir.eachFile { File file ->
            if (file.directory) {
                eachFileRecurse(file, fileProcessor)
            } else {
                fileProcessor(file)
            }
        }
    }

}