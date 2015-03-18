package frege.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class FregePlugin implements Plugin<Project> {

    void apply(Project project) {
        // Workaround to build proper jars on Windows, see https://github.com/Frege/frege-gradle-plugin/issues/9
        System.setProperty("file.encoding", "UTF-8")

        project.apply(plugin: 'base')
        def e = (FregePluginExtension) project.extensions.create("frege", FregePluginExtension)

        project.task('compileFrege', type: FregeTask, group: 'Build') << {

        }
        project.tasks.classes.dependsOn("compileFrege")

        def replTask = project.task('fregeRepl', type: FregeReplTask, group: 'Tools', dependsOn: 'compileFrege')
        replTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        def checkTask = project.task('quickCheck', type: FregeQuickCheckTask, group: 'Tools', dependsOn: 'compileFrege')
        checkTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        project.task('fregeDoc', type: FregeDocTask, group: 'Tools', dependsOn: 'compileFrege')

        project.task('fregeNativeGen', type: NativeGenTask, group: 'Tools')

    }

}
