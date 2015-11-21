package frege.gradle.plugins

import frege.gradle.tasks.FregeDoc
import frege.gradle.tasks.FregeNativeGen
import frege.gradle.tasks.FregeQuickCheck
import frege.gradle.tasks.FregeRepl
import org.gradle.api.Plugin
import org.gradle.api.Project

class FregePlugin implements Plugin<Project> {

    Project project

    void apply(Project project) {
        this.project = project

        project.plugins.apply(FregeBasePlugin)
        project.plugins.apply("java")

        def replTask = project.task('fregeRepl', type: FregeRepl, group: 'Tools', dependsOn: 'compileFrege')
        replTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        def checkTask = project.task('fregeQuickCheck', type: FregeQuickCheck, group: 'Verification', dependsOn: 'testClasses')
        checkTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        project.tasks.test.dependsOn("fregeQuickCheck")

        project.task('fregeDoc', type: FregeDoc, group: 'Documentation', dependsOn: 'compileFrege')

        project.task('fregeNativeGen', type: FregeNativeGen, group: 'Tools')

    }

}
