package frege.gradle

import groovy.transform.TypeChecked
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import fj.data.Option
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention

import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention

import javax.inject.Inject



import static org.gradle.api.plugins.ApplicationPlugin.TASK_RUN_NAME
import static org.gradle.api.plugins.ApplicationPlugin.TASK_START_SCRIPTS_NAME
import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CONFIGURATION_NAME



//@TypeChecked
class FregePlugin implements Plugin<Project> {


    public static final String FREGE_PLUGIN_NAME = 'frege'
    public static final String FREGE_CONFIGURATION_NAME = FREGE_PLUGIN_NAME



    Project project
    FileResolver fileResolver
    Configuration fregeConfiguration
    FregePluginExtension pluginExtension

    CompileTask compileTask


    @Inject
    FregePlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }


    void apply(Project project) {

        this.project = project
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(ApplicationPlugin)
        configureSourceSetDefaults(project.plugins.getPlugin(JavaBasePlugin))
        configureFregeConfigurationAndClasspath()
//
        configureApplicationPlugin()
        addFregePluginExtension()

        // Workaround to build proper jars on Windows, see https://github.com/Frege/frege-gradle-plugin/issues/9
        System.setProperty("file.encoding", "UTF-8")

        project.apply(plugin: 'base')
//        def e = (FregePluginExtension) project.extensions.create("frege", FregePluginExtension)

//        project.task('compileFrege', type: CompileTask, group: 'Build')
        project.tasks["classes"].dependsOn("compileFrege")
        project.tasks["compileFrege"].dependsOn("compileJava")

//        project.task('compileTestFrege', type: CompileTask, group: 'Build') {
//            outputDir = CompileTask.deduceTestClassesDir(project)
//            fregePaths = Option.fromNull(
//                CompileTask.deduceClassesDir(project)
//            ).map{d -> [d]}.orSome([])
//        }
        project.tasks.testClasses.dependsOn("compileTestFrege")
        project.tasks.compileTestFrege.dependsOn("compileTestJava")

        def replTask = project.task('fregeRepl', type: ReplTask, group: 'Tools', dependsOn: 'compileFrege')
        replTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks

        def checkTask = project.task('fregeQuickCheck', type: QuickCheckTask, group: 'Tools', dependsOn: 'compileFrege')

        checkTask.outputs.upToDateWhen { false } // always run, regardless of up to date checks
        project.tasks.fregeQuickCheck.dependsOn("testClasses")
        project.tasks.test.dependsOn("fregeQuickCheck")

        project.task('fregeDoc', type: DocTask, group: 'Tools', dependsOn: 'compileFrege')

        project.task('fregeNativeGen', type: NativeGenTask, group: 'Tools')

    }


    private void configureSourceSetDefaults(JavaBasePlugin javaBasePlugin) {
        project.convention.getPlugin(JavaPluginConvention).sourceSets.all { sourceSet ->
            def fregeSourceSet = new FregeSourceSet(sourceSet.displayName, fileResolver)
            new DslObject(sourceSet).convention.plugins.put(FREGE_PLUGIN_NAME, fregeSourceSet)

            fregeSourceSet.frege.srcDir("src/${sourceSet.name}/frege")
            def compileTaskName = sourceSet.getCompileTaskName(FREGE_PLUGIN_NAME)

            compileTask = project.tasks.create(compileTaskName, CompileTask)
            javaBasePlugin.configureForSourceSet(sourceSet, compileTask)
            compileTask.dependsOn(sourceSet.compileJavaTaskName)
            compileTask.setDescription("Compiles the ${sourceSet.name} Frege sources.")
            compileTask.setSource(fregeSourceSet.frege)

            project.tasks.getByName(sourceSet.classesTaskName).dependsOn(compileTaskName)
        }
    }


    private void configureApplicationPlugin() {
        def run = project.tasks.getByName(TASK_RUN_NAME)
        run.conventionMapping.main = { "${compileTask.module}".toString() }
        run.doFirst {
            ensureMainModuleConfigured()
        }

        def startScripts = project.tasks.getByName(TASK_START_SCRIPTS_NAME)
        startScripts.conventionMapping.mainClassName = { "${compileTask.module}".toString() }
        startScripts.doFirst {
            ensureMainModuleConfigured()
        }
    }


    private void ensureMainModuleConfigured() {
        // TODO: no op - should probably be removed
//        if (!pluginExtension.module) {
//            throw new InvalidUserDataException('You must specify the mainModule using frege extension.')
//        }
    }


    private void configureFregeConfigurationAndClasspath() {
        fregeConfiguration = project.configurations.create(FREGE_CONFIGURATION_NAME)
            .setVisible(false)
            .setDescription('The Frege libraries to be used for this Frege project.')

        project.configurations.getByName(RUNTIME_CONFIGURATION_NAME).extendsFrom(fregeConfiguration)
        project.tasks.withType(CompileTask) { CompileTask fregeCompile ->
            // TODO

//            fregeCompile.conventionMapping.map(CompileTask.FREGE_CLASSPATH_FIELD) { fregeConfiguration }
        }
    }

    private void addFregePluginExtension() {
        pluginExtension = project.extensions.create(FREGE_PLUGIN_NAME, FregePluginExtension)
    }

}
