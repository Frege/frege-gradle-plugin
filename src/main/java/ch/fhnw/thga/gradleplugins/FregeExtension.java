package ch.fhnw.thga.gradleplugins;

import javax.inject.Inject;

import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import groovy.transform.Internal;

public abstract class FregeExtension {
    public static final String DEFAULT_FREGE_EXTENSION_NAMESPACE = "frege";
    public static final String FREGE_VERSION_BUILD_FILE_KEY = "fregeVersion";
    public static final String FREGE_RELEASE_BUILD_FILE_KEY = "fregeRelease";
    public static final String FREGE_COMPILER_BUILD_FILE_KEY = "fregeCompilerPath";

    public abstract Property<String> getFregeVersion();

    public abstract Property<String> getFregeRelease();

    public abstract RegularFileProperty getFregeCompilerPath();

    @Internal
    public Provider<String> getDefaultJarName() {
        return getFregeVersion().map(version -> version + ".jar");
    }

    @Inject
    public FregeExtension(ProjectLayout projectLayout) {
        getFregeCompilerPath().set(projectLayout.getProjectDirectory().file("lib/frege" + getDefaultJarName()));
    }
}
