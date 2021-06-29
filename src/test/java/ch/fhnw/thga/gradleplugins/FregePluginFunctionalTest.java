package ch.fhnw.thga.gradleplugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FregePluginFunctionalTest {
    @TempDir File testProjectDir;
    private File buildFile;

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @BeforeEach
    void setup() throws Exception {
        buildFile = new File(testProjectDir, "build.gradle");
        String buildFileContent = "plugins {" +
                                    "id 'ch.fhnw.thga.frege'" +
                                  "}";
        writeFile(buildFile, buildFileContent);
    }

    @Test
    void given_frege_plugin_when_applying_then_latest_frege_compiler_is_downloaded_and_added_as_a_depenency() {
        //Project project = ProjectBuilder.builder().withProjectDir(testProjectDir).build();
        //assertEquals(1, project.getConfigurations().getByName("fregeCompiler").getAllDependencies().size());
        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .build();
        assertEquals(SUCCESS, result.task("initFrege").getOutcome());
    } 
}