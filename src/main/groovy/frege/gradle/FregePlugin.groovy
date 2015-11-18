package frege.gradle
import fj.data.Option
import org.gradle.api.Plugin
import org.gradle.api.Project
//@TypeChecked
class FregePlugin implements Plugin<Project> {

    void apply(Project project) {

        project.plugins.apply(FregeBasePlugin)

        project.task('compileFrege', type: CompileTask, group: 'Build', dependsOn: "compileJava") {
            module = CompileTask.deduceSourceDir(project).absolutePath
        }
        project.tasks.classes.dependsOn("compileFrege")

        project.task('compileTestFrege', type: CompileTask, group: 'Build', dependsOn: "compileTestJava") {
            module = CompileTask.deduceTestSourceDir(project).absolutePath
            outputDir = CompileTask.deduceTestClassesDir(project)
            fregePaths = Option.fromNull(CompileTask.deduceClassesDir(project))
                    .map{d -> [d]}.orSome([])
        }
        project.tasks.testClasses.dependsOn("compileTestFrege")

        def replTask = project.task('fregeRepl', type: ReplTask, group: 'Tools', dependsOn: 'compileFrege')
        replTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        def checkTask = project.task('fregeQuickCheck', type: QuickCheckTask, group: 'Verification', dependsOn: 'testClasses')
        checkTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        project.tasks.test.dependsOn("fregeQuickCheck")

        project.task('fregeDoc', type: DocTask, group: 'Documentation', dependsOn: 'compileFrege')

        project.task('fregeNativeGen', type: NativeGenTask, group: 'Tools')

    }

}
