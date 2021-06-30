package ch.fhnw.thga.gradleplugins;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class SetupFregeCompilerTask extends DefaultTask {

    @Input
    public abstract Property<String> getFregeVersion();

    @Input
    public abstract Property<String> getFregeRelease();

    @OutputFile
    public abstract RegularFileProperty getFregeCompilerPath();

    @TaskAction
    public void downloadFregeCompiler() {
            try {
                ReadableByteChannel readChannel = Channels.newChannel(
                    new URL("https://github.com/Frege/frege/releases/download/" +
                        getFregeRelease().get() + "/frege" + getFregeVersion().get() + ".jar").openStream());
                FileOutputStream fregeCompilerDestinationPath = new FileOutputStream(getFregeCompilerPath().get().getAsFile().getAbsolutePath());
                FileChannel writeChannel = fregeCompilerDestinationPath.getChannel();
                writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
                System.out.println("Successfully downloaded compiler to" + getFregeCompilerPath().get());
            } catch (IOException e) {
                throw new GradleException(e.getMessage());
            }
    }
}
