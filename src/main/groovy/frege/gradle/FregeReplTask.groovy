package frege.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

class FregeReplTask extends DefaultTask {

    static String DEFAULT_SRC_DIR        = "src/main/frege"     // TODO: should this come from a source set?
    static String DEFAULT_CLASSES_SUBDIR = "classes/main"       // TODO: should this come from a convention?

    @Optional @InputDirectory
    File sourceDir = new File(project.projectDir, DEFAULT_SRC_DIR).exists() ?  new File(project.projectDir, DEFAULT_SRC_DIR) : null

    @Optional @OutputDirectory
    File targetDir = new File(project.buildDir, DEFAULT_CLASSES_SUBDIR)

    @TaskAction
    void openFregeRepl() {

        if (sourceDir != null && !sourceDir.exists() ) {
            def currentDir = new File('.')
            logger.info "Intended source dir '${sourceDir.absolutePath}' doesn't exist. Using current dir '${currentDir.absolutePath}' ."
            sourceDir = currentDir
        }

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.repl.FregeRepl")
        action.workingDir = sourceDir ?: project.projectDir
        action.standardInput = System.in
        action.setClasspath(project.files(project.configurations.runtime ) + project.files(targetDir.absolutePath))

        action.execute()
    }

}