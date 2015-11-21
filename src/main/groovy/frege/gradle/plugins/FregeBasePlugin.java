package frege.gradle.plugins;

import frege.gradle.DefaultFregeSourceSet;
import frege.gradle.tasks.FregeCompile;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;

import javax.inject.Inject;
import java.util.concurrent.Callable;

public class FregeBasePlugin implements Plugin<Project> {
    private FileResolver fileResolver;

    private static String EXTENSION_NAME = "frege";
    private FregePluginExtension fregePluginExtension;
    private Project project;

    @Inject
    public FregeBasePlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    @Override
    public void apply(Project project) {
        // Workaround to build proper jars on Windows, see https://github.com/Frege/frege-gradle-plugin/issues/9
        this.project = project;
        System.setProperty("file.encoding", "UTF-8");
        project.getPluginManager().apply(JavaBasePlugin.class);
        fregePluginExtension = project.getExtensions().create(EXTENSION_NAME, FregePluginExtension.class);
        JavaBasePlugin javaBasePlugin = project.getPlugins().getPlugin(JavaBasePlugin.class);

        configureCompileDefaults(new FregeRuntime(project));
        configureSourceSetDefaults(javaBasePlugin);
    }


    private void configureSourceSetDefaults(final JavaBasePlugin javaBasePlugin) {
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(new Action<SourceSet>() {
            public void execute(SourceSet sourceSet) {
                final DefaultFregeSourceSet fregeSourceSet = new DefaultFregeSourceSet(((DefaultSourceSet) sourceSet).getDisplayName(), fileResolver);
                new DslObject(sourceSet).getConvention().getPlugins().put("frege", fregeSourceSet);

                final String defaultSourcePath = String.format("src/%s/frege", sourceSet.getName());
                fregeSourceSet.getFrege().srcDir(defaultSourcePath);
                sourceSet.getResources().getFilter().exclude(new Spec<FileTreeElement>() {
                    public boolean isSatisfiedBy(FileTreeElement element) {
                        return fregeSourceSet.getFrege().contains(element.getFile());
                    }
                });
                sourceSet.getAllJava().source(fregeSourceSet.getFrege());
                sourceSet.getAllSource().source(fregeSourceSet.getFrege());

                String compileTaskName = sourceSet.getCompileTaskName("frege");
                FregeCompile compile = project.getTasks().create(compileTaskName, FregeCompile.class);
                compile.setModule(project.file(defaultSourcePath).getAbsolutePath());
                javaBasePlugin.configureForSourceSet(sourceSet, compile);
                compile.dependsOn(sourceSet.getCompileJavaTaskName());
                compile.setDescription(String.format("Compiles the %s Frege source.", sourceSet.getName()));
                compile.setSource(fregeSourceSet.getFrege());
                project.getTasks().getByName(sourceSet.getClassesTaskName()).dependsOn(compileTaskName);
            }
        });
    }

    private void configureCompileDefaults(final FregeRuntime fregeRuntime) {
        this.project.getTasks().withType(FregeCompile.class, new Action<FregeCompile>() {
            public void execute(final FregeCompile compile) {
                compile.getConventionMapping().map("fregeClasspath", new Callable() {
                    public Object call() throws Exception {
                        return fregeRuntime.inferFregeClasspath(compile.getClasspath());
                    }

                });
            }
        });
    }

}
