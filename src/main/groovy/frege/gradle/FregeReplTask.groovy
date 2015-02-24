package frege.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

import javax.management.relation.Relation

class FregeReplTask extends DefaultTask {

    static String DEFAULT_SRC_DIR        = "src/main/frege"     // TODO: should this come from a source set?

    @Optional @InputDirectory
    File sourceDir = new File(project.projectDir, DEFAULT_SRC_DIR)

    @TaskAction
    void openFregeRepl() {

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
        action.setClasspath(project.files(project.configurations.compile ))

        action.execute()
    }

}