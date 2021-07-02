package ch.fhnw.thga.gradleplugins;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;


public abstract class FregeExtension {
    public static final String DEFAULT_FREGE_EXTENSION_NAMESPACE = "frege";
    public static final String FREGE_VERSION_BUILD_FILE_KEY = "fregeVersion";
    public static final String FREGE_RELEASE_BUILD_FILE_KEY = "fregeRelease";
    public static final String FREGE_COMPILER_OUTPUT_DIRECTORY_KEY = "fregeCompilerOutputDirectory";

    public abstract Property<String> getFregeVersion();

    public abstract Property<String> getFregeRelease();

    public abstract DirectoryProperty getFregeCompilerOutputDirectory();

    public abstract DirectoryProperty getFregeMainSourceDir();
    
    public abstract DirectoryProperty getFregeOutputDir();

    @Inject
    public FregeExtension(ProjectLayout projectLayout) {
        getFregeCompilerOutputDirectory().convention(projectLayout.getProjectDirectory().dir("lib"));
        // TODO: change to projectDir/src/main/frege
        getFregeMainSourceDir().convention(projectLayout.getProjectDirectory());
        getFregeOutputDir().convention(projectLayout.getBuildDirectory().dir("classes/main/frege"));
    }

}
