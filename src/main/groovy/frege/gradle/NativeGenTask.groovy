package frege.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

class NativeGenTask extends DefaultTask {

    @Optional
    @InputFile
    File typesFile = new File(project.projectDir, "types.properties")

    @Input
    String className = null

    @Optional
    @OutputFile
    File outputFile = new File(project.buildDir, "generated/frege/NativeGenOutput.fr")


    @TaskAction
    void gen() {

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.nativegen.Main")
        action.workingDir = project.projectDir
        action.standardInput = System.in
        action.standardOutput = outputFile.newOutputStream()
        action.errorOutput = System.err
        action.setClasspath(project.files(project.configurations.compile) + project.files("$project.buildDir/classes/main"))

        def args = []
        args << className
        args << typesFile.absolutePath
        action.args args
        action.execute()
    }


}
