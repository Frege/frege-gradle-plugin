package ch.fhnw.thga.gradleplugins;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.apache.log4j.Logger;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class FregeInitTask extends DefaultTask {
    private final static Logger LOGGER = Logger.getLogger(FregeInitTask.class.getName());
    private static final String DEFAULT_FREGE_VERSION = "3.25.84";
    private static final String DEFAULT_FREGE_RELEASE = "3.25alpha";

    @Input
    public abstract Property<String> getFregeVersion();

    @Input
    public abstract Property<String> getFregeRelease();

    @OutputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    public FregeInitTask() {
        getFregeVersion().convention(DEFAULT_FREGE_VERSION);
        getFregeRelease().convention(DEFAULT_FREGE_RELEASE);
        getFregeCompilerJar().convention(getProject().getLayout().getBuildDirectory().file("lib/frege" + getFregeVersion() + ".jar"));
    }

    @TaskAction
    public void downloadFregeCompiler() {
            try {
                ReadableByteChannel readChannel = Channels.newChannel(
                    new URL("https://github.com/Frege/frege/releases/download/" +
                        getFregeRelease().get() + "/frege" + getFregeVersion() + ".jar").openStream());
                FileOutputStream fregeCompilerDestinationPath = new FileOutputStream(getFregeCompilerJar().get().getAsFile().getAbsolutePath());
                FileChannel writeChannel = fregeCompilerDestinationPath.getChannel();
                writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e.getCause());
            }
    }
}
