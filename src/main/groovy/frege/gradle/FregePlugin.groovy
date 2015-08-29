package frege.gradle

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
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

class FregePlugin implements Plugin<Project> {

    public static final String FREGE_PLUGIN_NAME = 'frege'
    public static final String FREGE_CONFIGURATION_NAME = FREGE_PLUGIN_NAME

    Project project
    FileResolver fileResolver
    Configuration fregeConfiguration
    FregePluginExtension pluginExtension

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

        configureApplicationPlugin()
        addFregePluginExtension()

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

    private void configureSourceSetDefaults(JavaBasePlugin javaBasePlugin) {
        project.convention.getPlugin(JavaPluginConvention).sourceSets.all { sourceSet ->
            def fregeSourceSet = new FregeSourceSet(sourceSet.displayName, fileResolver)
            new DslObject(sourceSet).convention.plugins.put(FREGE_PLUGIN_NAME, fregeSourceSet)

            fregeSourceSet.frege.srcDir("src/${sourceSet.name}/frege")

            def compileTaskName = sourceSet.getCompileTaskName(FREGE_PLUGIN_NAME)

            def fregeCompile = project.tasks.create(compileTaskName, FregeTask)
            javaBasePlugin.configureForSourceSet(sourceSet, fregeCompile)
            fregeCompile.dependsOn(sourceSet.compileJavaTaskName)
            fregeCompile.setDescription("Compiles the ${sourceSet.name} Frege sources.")
            fregeCompile.setSource(fregeSourceSet.frege)

            project.tasks.getByName(sourceSet.classesTaskName).dependsOn(compileTaskName)
        }
    }

    private void configureApplicationPlugin() {
        def run = project.tasks.getByName(TASK_RUN_NAME)
        run.conventionMapping.main = { "${pluginExtension.mainModule}".toString() }
        run.doFirst {
            ensureMainModuleConfigured()
        }

        def startScripts = project.tasks.getByName(TASK_START_SCRIPTS_NAME)
        startScripts.conventionMapping.mainClassName = { "${pluginExtension.mainModule}".toString() }
        startScripts.doFirst {
            ensureMainModuleConfigured()
        }
    }

    private void ensureMainModuleConfigured() {
        if (!pluginExtension.mainModule) {
            throw new InvalidUserDataException('You must specify the mainModule using frege extension.')
        }
    }

    private void configureFregeConfigurationAndClasspath() {
        fregeConfiguration = project.configurations.create(FREGE_CONFIGURATION_NAME)
                                   .setVisible(false)
                                   .setDescription('The Frege libraries to be used for this Frege project.')

        project.configurations.getByName(RUNTIME_CONFIGURATION_NAME).extendsFrom(fregeConfiguration)

        project.tasks.withType(FregeTask) { FregeTask fregeCompile ->
            fregeCompile.conventionMapping.map(FregeTask.FREGE_CLASSPATH_FIELD) { fregeConfiguration }
        }
    }

    private void addFregePluginExtension() {
        pluginExtension = project.extensions.create(FREGE_PLUGIN_NAME, FregePluginExtension)
    }
}
