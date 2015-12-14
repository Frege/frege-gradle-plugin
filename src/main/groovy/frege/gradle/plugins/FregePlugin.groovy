package frege.gradle.plugins
import frege.gradle.tasks.FregeDoc
import frege.gradle.tasks.FregeNativeGen
import frege.gradle.tasks.FregeQuickCheck
import frege.gradle.tasks.FregeRepl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.util.PatternSet

class FregePlugin implements Plugin<Project> {

    Project project

    void apply(Project project) {
        this.project = project

        project.plugins.apply(FregeBasePlugin)
        project.plugins.apply("java")

        def replTask = project.task('fregeRepl', type: FregeRepl, group: 'frege', dependsOn: 'compileFrege')
        replTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        def checkTask = project.task('fregeQuickCheck', type: FregeQuickCheck, group: 'frege', dependsOn: 'testClasses')
        checkTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        project.tasks.test.dependsOn("fregeQuickCheck")


        configureFregeDoc()

        project.task('fregeNativeGen', type: FregeNativeGen, group: 'frege')

    }

    def configureFregeDoc() {
        FregeDoc fregeDoc = project.tasks.create('fregeDoc', FregeDoc)
        fregeDoc.group = 'frege'
        fregeDoc.dependsOn "compileFrege" // TODO remove
        SourceSet mainSourceSet = project.sourceSets.main
        fregeDoc.module = mainSourceSet.output.classesDir.absolutePath
        fregeDoc.classpath = mainSourceSet.runtimeClasspath
    }


    FileCollection findJavaModulesToExclude(SourceSet sourceSet) {
        return sourceSet.allJava.asFileTree.matching { PatternSet pattern -> pattern.include("**/*.java")}
    }
}
