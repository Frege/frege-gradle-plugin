package ch.fhnw.thga.gradleplugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class FregePlugin implements Plugin<Project> {
    public static final String SETUP_FREGE_TASK_NAME = "setupFrege";
    public static final String COMPILE_FREGE_TASK_NAME = "compileFrege";
    public static final String FREGE_PLUGIN_ID = "ch.fhnw.thga.frege";
    public static final String FREGE_EXTENSION_NAME = "frege";

    @Override
    public void apply(Project project) {
        FregeExtension extension = project.getExtensions().create(FREGE_EXTENSION_NAME, FregeExtension.class);
        TaskProvider<SetupFregeTask> setupFregeCompilerTask = project.getTasks().register(SETUP_FREGE_TASK_NAME,
                SetupFregeTask.class, task -> {
                    task.getVersion().set(extension.getVersion());
                    task.getRelease().set(extension.getRelease());
                    task.getDownloadDir().set(extension.getCompilerDownloadDir());
                });

        project.getTasks().register(COMPILE_FREGE_TASK_NAME, CompileFregeTask.class, task -> {
            task.dependsOn(setupFregeCompilerTask);
            task.getFregeCompilerJar().set(setupFregeCompilerTask.get().getFregeCompilerOutputPath());
            task.getFregeMainSourceDir().set(extension.getMainSourceDir());
            task.getFregeOutputDir().set(extension.getOutputDir());
        });
    }
}
