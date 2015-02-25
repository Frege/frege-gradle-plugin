package frege.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class FregePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.apply(plugin: 'base')
        def e = (FregePluginExtension) project.extensions.create("frege", FregePluginExtension)

        project.task('compileFrege', type: FregeTask, group: 'Build') << {

        }
        project.tasks.classes.dependsOn("compileFrege")

        def replTask = project.task('fregeRepl', type: FregeReplTask, group: 'Tools', dependsOn: 'classes')
        replTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        def checkTask = project.task('quickCheck', type: FregeQuickCheckTask, group: 'Tools', dependsOn: 'classes')
        checkTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        project.task('fregeNativeGen', type: NativeGenTask)

    }

}
