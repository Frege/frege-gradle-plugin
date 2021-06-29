package ch.fhnw.thga.gradleplugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

public class FregePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().create("fregeInit", FregeInitTask.class);
        Configuration fregeCompiler = project.getConfigurations().create("fregeCompiler", c -> {
            c.setVisible(false);
            c.setCanBeConsumed(false);
            c.setCanBeResolved(true);
            c.setDescription("The frege compiler");
            c.defaultDependencies(d -> d.add(project.getDependencies().create("frege3.25.84.jar")));
        });
    }
}
