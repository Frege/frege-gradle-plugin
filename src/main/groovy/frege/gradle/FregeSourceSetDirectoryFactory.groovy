package frege.gradle

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.util.GradleVersion

public class FregeSourceSetDirectoryFactory {
    private final boolean useFactory;
    private final FileResolver fileResolver
    private final ProjectInternal project

    public FregeSourceSetDirectoryFactory(ProjectInternal project, FileResolver fileResolver) {
        this.fileResolver = fileResolver
        this.project = project
        this.useFactory = GradleVersion.current().compareTo(GradleVersion.version("2.12")) >= 0;

    }

    public SourceDirectorySet newSourceSetDirectory(String displayName) {
        if (useFactory) {
            SourceDirectorySetFactory factory = project.getServices().get(SourceDirectorySetFactory.class);
            return factory.create(displayName);
        } else {
            return new DefaultSourceDirectorySet(displayName, fileResolver);
        }
    }
}
