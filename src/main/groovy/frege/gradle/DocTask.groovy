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

    static String DEFAULT_SRC_DIR     = "src/main/frege"     // TODO: should this come from a source set?
    static String DEFAULT_DOCS_SUBDIR = "docs/frege"       // TODO: should this come from a convention?

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
        if (verbose) args << '-v'
        args << '-d' << targetDir.absolutePath
        if (exclude) args << '-x' << exclude
        args << module

        action.args args
        action.execute()
    }

}

