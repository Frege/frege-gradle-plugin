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

        def oFR = project.task('openFregeRepl', type: FregeReplTask, group: 'Runtime', dependsOn: 'classes')
        oFR.outputs.upToDateWhen { false }


        project.task('fregeRepl', type: ReplTask)
        project.task('fregeNativeGen', type: NativeGenTask)

    }

}
