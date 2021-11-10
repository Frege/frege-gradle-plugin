package ch.fhnw.thga.gradleplugins;

import java.util.List;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class CompileFregeTask extends DefaultTask {
    private final JavaExec javaExec;

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @InputDirectory
    public abstract DirectoryProperty getFregeMainSourceDir();

    @OutputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @Inject
    public CompileFregeTask(ObjectFactory objectFactory) {
        javaExec = objectFactory.newInstance(JavaExec.class);
    }

    @TaskAction
    public void compileFrege() {
        String fregeMainSourceDir = getFregeMainSourceDir().getAsFile().get().getAbsolutePath();
        List<String> args = List.of("-v", "-d", getFregeOutputDir().get().getAsFile().getAbsolutePath(), "-sp",
                fregeMainSourceDir, fregeMainSourceDir);
        javaExec.setClasspath(getProject().files(getFregeCompilerJar())).setArgs(args).exec();
    }
}
