package frege.gradle.plugins;

import com.google.common.collect.Lists;
import org.gradle.api.Buildable;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.LazilyInitializedFileCollection;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;

import java.io.File;
import java.util.List;

public class FregeRuntime {

    private final Project project;

    public FregeRuntime(Project project) {
        this.project = project;
    }


    public FileCollection inferFregeClasspath(final Iterable<File> classpath) {
        return new LazilyInitializedFileCollection() {
            public String getDisplayName() {
                return "Frege runtime classpath";
            }

            public FileCollection createDelegate() {
                final FregeJarFile fregeJar = FregeRuntime.this.findFregeJarFile(classpath);
                if (fregeJar == null) {
                    throw new GradleException(String.format("Cannot infer Frege class path because no Frege Jar was found on class path: %s", classpath));
                }
                String notation = fregeJar.getDependencyNotation();
                List<Dependency> dependencies = Lists.newArrayList();
                dependencies.add(project.getDependencies().create(notation));
                return project.getConfigurations().detachedConfiguration(dependencies.toArray(new Dependency[dependencies.size()]));
            }

            public void visitDependencies(TaskDependencyResolveContext context) {
                if (classpath instanceof Buildable) {
                    context.add(classpath);
                }
            }

        };
    }

    private FregeJarFile findFregeJarFile(Iterable<File> classpath) {
        if (classpath == null) {
            return null;
        }
        for (File file : classpath) {
            FregeJarFile fregeJar = FregeJarFile.parse(file);
            if (fregeJar != null) {
                return fregeJar;
            }
        }
        return null;
    }

}
