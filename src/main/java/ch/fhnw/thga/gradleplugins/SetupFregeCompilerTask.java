package ch.fhnw.thga.gradleplugins;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class SetupFregeCompilerTask extends DefaultTask {
    public static final Logger LOGGER = Logging.getLogger(SetupFregeCompilerTask.class);

    private static final String FREGE_GITHUB_URL_PREFIX = "https://github.com/Frege/frege/releases/download";

    @Input
    public abstract Property<String> getFregeVersion();

    @Input
    public abstract Property<String> getFregeRelease();

    @Internal
    public abstract DirectoryProperty getFregeCompilerOutputDirectory();

    @Internal
    public Provider<String> getFregeVersionJarName() {
        return getFregeVersion().map(version -> "frege" + version + ".jar");
    }

    @Internal
    public Provider<String> getDownloadUrl() {
        return getFregeVersionJarName()
                .map(name -> String.join("/", FREGE_GITHUB_URL_PREFIX, getFregeRelease().get(), name));
    }

    @OutputFile
    public Provider<RegularFile> getFregeCompilerOutputPath() {
        return getFregeCompilerOutputDirectory().file(getFregeVersionJarName());
    }

    @TaskAction
    public void downloadFregeCompiler() {
        String fregeCompilerOutputPath = getFregeCompilerOutputPath().get().getAsFile().getAbsolutePath();
        try (ReadableByteChannel readChannel = Channels.newChannel(new URL(getDownloadUrl().get()).openStream());
                FileOutputStream fregeCompilerOutputStream = new FileOutputStream(fregeCompilerOutputPath);) {
            FileChannel writeChannel = fregeCompilerOutputStream.getChannel();
            writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
            LOGGER.lifecycle(String.format("Successfully downloaded %s to: %s", getFregeVersionJarName().get(), fregeCompilerOutputPath));
        } catch (IOException e) {
            throw new GradleException(e.getMessage());
        }
    }
}
