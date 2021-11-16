package ch.fhnw.thga.gradleplugins;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class CompileFregeTask extends DefaultTask {
    private final JavaExec javaExec;

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @InputDirectory
    public abstract DirectoryProperty getFregeMainSourceDir();

    @Input
    public abstract ListProperty<String> getFregeCompilerFlags();

    @OutputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @Internal
    public final Provider<String> getFregeMainSourcePath() {
        return getFregeMainSourceDir().map(srcDir -> srcDir.getAsFile().getAbsolutePath());
    }

    @Internal
    public final Provider<List<String>> getSourcePathArg() {
        return getFregeMainSourcePath().map(srcPath -> List.of("-sp", srcPath));
    }

    @Inject
    public CompileFregeTask(ObjectFactory objectFactory) {
        javaExec = objectFactory.newInstance(JavaExec.class);
    }

    @TaskAction
    public void compileFrege() {
        List<String> directoryArg = List.of("-d", getFregeOutputDir().getAsFile().get().getAbsolutePath());
        List<String> compilerArgs = Stream
                .of(getFregeCompilerFlags().get(), directoryArg, getSourcePathArg().get(),
                        List.of(getFregeMainSourcePath().get()))
                .flatMap(Collection::stream).collect(Collectors.toList());
        javaExec.setClasspath(getProject().files(getFregeCompilerJar())).setArgs(compilerArgs).exec();
    }
}
