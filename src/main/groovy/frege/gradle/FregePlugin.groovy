package frege.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import fj.data.Option

class FregePlugin implements Plugin<Project> {

    void apply(Project project) {
        // Workaround to build proper jars on Windows, see https://github.com/Frege/frege-gradle-plugin/issues/9
        System.setProperty("file.encoding", "UTF-8")

        project.apply(plugin: 'base')
        def e = (FregePluginExtension) project.extensions.create("frege", FregePluginExtension)

        project.task('compileFrege', type: CompileTask, group: 'Build') << {

        }
        project.tasks.classes.dependsOn("compileFrege")
        project.tasks.compileFrege.dependsOn("compileJava")

        project.task('compileTestFrege', type: CompileTask, group: 'Build') {
//            sourcePaths = [CompileTask.deduceTestSrcDir(project)]
            outputDir = CompileTask.deduceTestClassesDir(project)
//            logger.info("compileTestFrege debug")
//            logger.info("projectDir ${project.projectDir}")
//            logger.info("defaultSrc ${CompileTask.DEFAULT_SRC_DIR}")
            fregePackageDirs = Option.fromNull(
                CompileTask.deduceClassesDir(project)
            ).map { d -> [d.absolutePath] }.orSome([])
        }
        project.tasks.testClasses.dependsOn("compileTestFrege")
        project.tasks.compileTestFrege.dependsOn("compileTestJava")

        def replTask = project.task('fregeRepl', type: ReplTask, group: 'Tools', dependsOn: 'compileFrege')
        replTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        def checkTask = project.task('fregeQuickCheck', type: QuickCheckTask, group: 'Tools', dependsOn: 'compileFrege')
        checkTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        project.task('fregeDoc', type: DocTask, group: 'Tools', dependsOn: 'compileFrege')

        project.task('fregeNativeGen', type: NativeGenTask, group: 'Tools')

    }

}
