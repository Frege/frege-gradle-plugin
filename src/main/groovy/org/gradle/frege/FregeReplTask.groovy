package org.gradle.frege

import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

import javax.management.relation.Relation

class FregeReplTask extends DefaultTask {

    static String DEFAULT_SRC_DIR        = "src/main/frege"     // TODO: should this come from a source set?

    @Optional @InputDirectory
    File replDir

    @Optional @InputDirectory
    File sourceDir = new File(project.projectDir, DEFAULT_SRC_DIR)

    @TaskAction
    void openFregeRepl() {

        if (! replDir) replDir = new File(System.properties.'user.home'.toString(), "/.frege/repl")

        def replJarFileNames = []

        if (! replDir.exists() ) {
            throw new StopActionException("REPL installation directory '${replDir.absolutePath}' does not exist. Cannot start the REPL.")
        }

        replDir.eachFileRecurse { file ->
            if (file.name ==~ /^(frege-|ecj-|jline).*\.jar$/) {
                replJarFileNames << file.absolutePath
            }
        }
        logger.debug "repl installation jar file names are ${replJarFileNames}"

        if (replJarFileNames.size() < 6) {
            throw new StopActionException("Found only ${replJarFileNames.size()} jars in REPL installation directory '${replDir.absolutePath}'. Cannot start the REPL.")
        }

        if (! sourceDir.exists() ) {
            def currentDir = new File('.')
            logger.info "Intended source dir '${sourceDir.absolutePath}' doesn't exist. Using current dir '${currentDir.absolutePath}' ."
            sourceDir = currentDir
        }


        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.repl.FregeRepl")
        action.workingDir = sourceDir
        action.standardInput = System.in
        action.setClasspath(project.files(project.configurations.compile , *replJarFileNames))

        action.execute()
    }

}