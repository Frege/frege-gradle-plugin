package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.GradleBuildFileConversionTest.createPluginsSection;
import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_DOWNLOAD_DIRECTORY;
import static ch.fhnw.thga.gradleplugins.FregePlugin.*;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class FregePluginFunctionalTest {
    private static final String NEW_LINE = System.lineSeparator();

    private final FregeDTOBuilder fregeBuilder = new FregeDTOBuilder();

    @TempDir
    File testProjectDir;
    private File buildFile;
    private File settingsFile;
    private Project project;

    private void writeFile(File destination, String content, boolean append) throws IOException {
        try (BufferedWriter output = new BufferedWriter(new FileWriter(destination, append))) {
            output.write(content);
        }
    }

    private void writeToFile(File destination, String content) throws IOException {
        writeFile(destination, content, false);
    }

    private void appendToFile(File destination, String content) throws IOException {
        writeFile(destination, "\n" + content, true);
    }

    private static String createFregeSection(FregeDTO fregeDTO) {
        return String.format("%s {\n  %s\n}", FREGE_EXTENSION_NAME, fregeDTO.toBuildFile());
    }

    private BuildResult runGradleTask(String taskName, String... args) {
        return GradleRunner.create().withProjectDir(testProjectDir).withPluginClasspath().withArguments(taskName)
                .build();
    }

    private BuildResult runAndFailGradleTask(String taskName, String... args) {
        return GradleRunner.create().withProjectDir(testProjectDir).withPluginClasspath().withArguments(taskName)
                .buildAndFail();
    }

    @BeforeAll
    void beforeAll() throws Exception {
        settingsFile = new File(testProjectDir, "settings.gradle");
        writeToFile(settingsFile, "rootProject.name='frege-plugin'");
        project = ProjectBuilder.builder().withProjectDir(testProjectDir).build();
        project.getPluginManager().apply(FREGE_PLUGIN_ID);

    }

    @BeforeEach
    void setup() throws Exception {
        buildFile = new File(testProjectDir, "build.gradle");
        writeToFile(buildFile, createPluginsSection(Stream.of(FREGE_PLUGIN_ID)));
    }

    @AfterEach
    void cleanup() {
        buildFile.delete();
    }

    @Nested
    @IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Setup_frege_task_works {

        @Test
        void given_minimal_build_file_config() throws Exception {
            String minimalBuildFileConfig = createFregeSection(
                    fregeBuilder.version("'3.25.84'").release("'3.25alpha'").build());
            appendToFile(buildFile, minimalBuildFileConfig);

            BuildResult result = runGradleTask(SETUP_FREGE_TASK_NAME);

            assertTrue(project.getTasks().getByName(SETUP_FREGE_TASK_NAME) instanceof SetupFregeTask);
            assertEquals(SUCCESS, result.task(":" + SETUP_FREGE_TASK_NAME).getOutcome());
            assertTrue(testProjectDir.toPath().resolve(Paths.get(DEFAULT_DOWNLOAD_DIRECTORY, "frege3.25.84.jar"))
                    .toFile().exists());
        }

        @Test
        void given_custom_frege_compiler_download_directory_in_build_file_config() throws Exception {
            String buildFileConfigWithCustomDownloadDir = createFregeSection(fregeBuilder.version("'3.25.84'")
                    .release("'3.25alpha'").compilerDownloadDir("layout.projectDirectory.dir('dist')").build());
            appendToFile(buildFile, buildFileConfigWithCustomDownloadDir);

            BuildResult result = runGradleTask(SETUP_FREGE_TASK_NAME);

            assertTrue(project.getTasks().getByName(SETUP_FREGE_TASK_NAME) instanceof SetupFregeTask);
            assertEquals(SUCCESS, result.task(":" + SETUP_FREGE_TASK_NAME).getOutcome());
            assertTrue(testProjectDir.toPath().resolve(Paths.get("dist", "frege3.25.84.jar")).toFile().exists());
        }
    }

    @Nested
    @IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Compile_frege_task_works {

        @Test
        void given_frege_code_in_src_main_frege_and_minimal_build_file_config() throws Exception {
            String fregeCode = "module ch.fhnw.thga.Completion where\n\n" + "complete :: Int -> (Int, String)\n"
                    + "complete i = (i, \"Frege rocks\")\n";
            Files.createDirectories(testProjectDir.toPath().resolve(Paths.get("src", "main", "frege")));
            File completionFr = testProjectDir.toPath().resolve(Paths.get("src", "main", "frege", "Completion.fr"))
                    .toFile();
            writeToFile(completionFr, fregeCode);
            String minimalBuildFileConfig = createFregeSection(
                    fregeBuilder.version("'3.25.84'").release("'3.25alpha'").build());
            appendToFile(buildFile, minimalBuildFileConfig);

            BuildResult result = runGradleTask(COMPILE_FREGE_TASK_NAME);

            assertTrue(project.getTasks().getByName(COMPILE_FREGE_TASK_NAME) instanceof CompileFregeTask);
            assertEquals(SUCCESS, result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
            assertTrue(new File(
                    testProjectDir.getAbsolutePath() + "/build/classes/main/frege/ch/fhnw/thga/Completion.java")
                            .exists());
            assertTrue(new File(
                    testProjectDir.getAbsolutePath() + "/build/classes/main/frege/ch/fhnw/thga/Completion.class")
                            .exists());
        }
    }

    @Nested
    @IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Run_frege_task_works {
        @Test
        void given_frege_file_with_main_function() throws Exception {
            String fregeCode = String.join(NEW_LINE, "module ch.fhnw.thga.Main where", NEW_LINE, NEW_LINE,
                    "  main = do", NEW_LINE, "    println \"Frege rocks\"", NEW_LINE);
            Files.createDirectories(testProjectDir.toPath().resolve(Paths.get("src", "main", "frege")));
            File mainFr = testProjectDir.toPath().resolve(Paths.get("src", "main", "frege", "Main.fr")).toFile();
            writeToFile(mainFr, fregeCode);
            String minimalBuildFileConfig = createFregeSection(
                    fregeBuilder.version("'3.25.84'").release("'3.25alpha'").mainModule("'ch.fhnw.thga.Main'").build());
            appendToFile(buildFile, minimalBuildFileConfig);

            BuildResult result = runGradleTask(RUN_FREGE_TASK_NAME);
            assertTrue(project.getTasks().getByName(RUN_FREGE_TASK_NAME) instanceof RunFregeTask);
            assertEquals(SUCCESS, result.task(":" + RUN_FREGE_TASK_NAME).getOutcome());
            assertTrue(result.getOutput().contains("Frege rocks"));
        }

        @Test
        void given_frege_file_without_main_function() throws Exception {
            String fregeCode = "module ch.fhnw.thga.Completion where\n\n" + "complete :: Int -> (Int, String)\n"
                    + "complete i = (i, \"Frege rocks\")\n";
            Files.createDirectories(testProjectDir.toPath().resolve(Paths.get("src", "main", "frege")));
            File completeFr = testProjectDir.toPath().resolve(Paths.get("src", "main", "frege", "Complete.fr")).toFile();
            writeToFile(completeFr, fregeCode);
            String minimalBuildFileConfig = createFregeSection(
                    fregeBuilder.version("'3.25.84'").release("'3.25alpha'").mainModule("'ch.fhnw.thga.Completion'").build());
            appendToFile(buildFile, minimalBuildFileConfig);

            BuildResult result = runAndFailGradleTask(RUN_FREGE_TASK_NAME);
            assertTrue(project.getTasks().getByName(RUN_FREGE_TASK_NAME) instanceof RunFregeTask);
            assertEquals(FAILED, result.task(":" + RUN_FREGE_TASK_NAME).getOutcome());
            assertTrue(result.getOutput().contains("Main method not found"));
        }
    }
}