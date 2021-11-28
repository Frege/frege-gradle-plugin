package ch.fhnw.thga.gradleplugins;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

public abstract class ReplFregeTask extends DefaultTask {
    public static final Logger LOGGER = Logging.getLogger(SetupFregeTask.class);
    public static final String REPL_MAIN_CLASS = "frege.repl.FregeRepl";

    private final JavaExec javaExec;

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @InputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @Input
    public abstract Property<String> getFregeDependencies();

    @Internal
    public final Provider<FileCollection> getClasspath() {
        return getFregeDependencies().map(depsClasspath -> {
            return depsClasspath.isEmpty() ? getProject().files(getFregeCompilerJar(), getFregeOutputDir())
                    : getProject().files(getFregeCompilerJar(), getFregeOutputDir(), depsClasspath);
        });
    }

    @Inject
    public ReplFregeTask(ObjectFactory objectFactory) {
        javaExec = objectFactory.newInstance(JavaExec.class);
    }

    @TaskAction
    public void startFregeRepl() {
        javaExec.setStandardInput(System.in);
        javaExec.getMainClass().set(REPL_MAIN_CLASS);
        javaExec.setClasspath(getClasspath().get()).exec();
    }
}
