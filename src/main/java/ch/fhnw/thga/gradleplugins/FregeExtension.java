package ch.fhnw.thga.gradleplugins;

import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;

public abstract class FregeExtension {
    public static final String DEFAULT_DOWNLOAD_DIRECTORY = "lib";
    public static final String DEFAULT_RELATIVE_OUTPUT_DIR = "classes/main/frege";

    public abstract Property<String> getVersion();

    public abstract Property<String> getRelease();

    public abstract Property<String> getMainModule();

    public abstract DirectoryProperty getCompilerDownloadDir();

    public abstract DirectoryProperty getMainSourceDir();

    public abstract DirectoryProperty getOutputDir();

    @Inject
    public FregeExtension(ProjectLayout projectLayout) {
        getCompilerDownloadDir().convention(projectLayout.getProjectDirectory().dir(DEFAULT_DOWNLOAD_DIRECTORY));
        getMainSourceDir().convention(projectLayout.getProjectDirectory());
        getOutputDir().convention(projectLayout.getBuildDirectory().dir(DEFAULT_RELATIVE_OUTPUT_DIR));
    }

}
