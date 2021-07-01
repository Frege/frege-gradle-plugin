package ch.fhnw.thga.gradleplugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class FregePlugin implements Plugin<Project> {
    public static final String SETUP_FREGE_COMPILER_TASK_NAME = "setupFregeCompiler";
    public static final String FREGE_PLUGIN_ID = "ch.fhnw.thga.frege";
    public static final String FREGE_EXTENSION_NAME = "frege";

    @Override
    public void apply(Project project) {
        FregeExtension extension = project.getExtensions().create(FREGE_EXTENSION_NAME, FregeExtension.class);
        project.getTasks().register(SETUP_FREGE_COMPILER_TASK_NAME, SetupFregeCompilerTask.class, task -> {
            task.getFregeVersion().set(extension.getFregeVersion());
            task.getFregeRelease().set(extension.getFregeRelease());
            task.getFregeCompilerOutputDirectory().set(extension.getFregeCompilerOutputDirectory());
        });
        //Configuration fregeCompiler = project.getConfigurations().create("fregeCompiler", c -> {
        //    c.setVisible(false);
        //    c.setCanBeConsumed(false);
        //    c.setCanBeResolved(true);
        //    c.setDescription("The frege compiler");
        //    c.defaultDependencies(d -> d.add(project.getDependencies().create("frege3.25.84.jar")));
        //});
    }
}
