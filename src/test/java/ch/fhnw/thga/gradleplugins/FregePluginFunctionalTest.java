package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_DOWNLOAD_DIRECTORY;
import static ch.fhnw.thga.gradleplugins.FregePlugin.COMPILE_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_EXTENSION_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_PLUGIN_ID;
import static ch.fhnw.thga.gradleplugins.FregePlugin.SETUP_FREGE_TASK_NAME;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FregePluginFunctionalTest {

    private final FregeDTOBuilder fregeBuilder = new FregeDTOBuilder();

    @TempDir
    File testProjectDir;
    private File buildFile;
    private File settingsFile;
    private Project project;

    private void writeFile(File destination, String content, boolean append) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination, append));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void writeToFile(File destination, String content) throws IOException {
        writeFile(destination, content, false);
    }

    private void appendToFile(File destination, String content) throws IOException {
        writeFile(destination, "\n" + content, true);
    }

    private static String buildFilePluginString(String pluginId) {
        return String.format("id '%s'", pluginId);
    }

    private static String createPluginsSection(Stream<String> pluginIds) {
        String plugins = pluginIds.map(pluginId -> buildFilePluginString(pluginId)).collect(Collectors.joining("\n  "));
        return String.format("plugins {\n  %s\n}\n", plugins);
    }

    private static String createFregeSection(FregeDTO fregeDTO) {
        return String.format("%s {\n  %s\n}", FREGE_EXTENSION_NAME, fregeDTO.toBuildFile());
    }

    private void assertGradleTaskOutcome(String buildFileTaskConfig, String taskName) throws Exception {
        appendToFile(buildFile, buildFileTaskConfig);
        BuildResult result = GradleRunner.create().withProjectDir(testProjectDir).withPluginClasspath()
                .withArguments(taskName).build();
        System.out.println(result.getOutput());
        assertEquals(SUCCESS, result.task(":" + taskName).getOutcome());
    }

    @BeforeEach
    void setup() throws Exception {
        buildFile = new File(testProjectDir, "build.gradle");
        settingsFile = new File(testProjectDir, "settings.gradle");
        writeToFile(buildFile, createPluginsSection(Stream.of(FREGE_PLUGIN_ID)));
        writeToFile(settingsFile, "rootProject.name='frege-plugin'");
        project = ProjectBuilder.builder().withProjectDir(testProjectDir).build();
        project.getPluginManager().apply(FREGE_PLUGIN_ID);
    }

    @Test
    void given_version_and_release_then_fregeDTO_can_be_converted_to_build_file_string() {
        FregeDTO fregeDTO = fregeBuilder.version("'3.25'").release("'3.25alpha'").build();
        String expected = "version = '3.25'\n  release = '3.25alpha'";
        assertEquals(expected, fregeDTO.toBuildFile());
    }

    @Test
    void given_version_release_and_compiler_download_dir_then_fregeDTO_can_be_converted_to_build_file_string() {
        FregeDTO fregeDTO = fregeBuilder.version("'3.25'").release("'3.25alpha'")
                .compilerDownloadDir("layout.projectDirectory.dir('dist')").build();
        String expected = "version = '3.25'\n  release = '3.25alpha'\n  compilerDownloadDir = layout.projectDirectory.dir('dist')";
        assertEquals(expected, fregeDTO.toBuildFile());
    }

    @Test
    void given_single_plugin_id_then_it_is_correctly_converted_to_build_file_string() {
        String pluginId = "frege";
        Stream<String> pluginIds = Stream.of(pluginId);
        String expected = "plugins {\n" + "  id '" + pluginId + "'\n" + "}\n";
        assertEquals(expected, createPluginsSection(pluginIds));
    }

    @Test
    void given_multiple_plugin_ids_then_they_are_correctly_converted_to_build_file_string() {
        String fregeId = "frege";
        String javaId = "java";
        Stream<String> pluginIds = Stream.of(fregeId, javaId);
        String expected = "plugins {\n" + "  id '" + fregeId + "'\n" + "  id '" + javaId + "'\n" + "}\n";
        assertEquals(expected, createPluginsSection(pluginIds));
    }

    @Test
    void given_setup_frege_compiler_task_when_frege_version_and_frege_release_is_specified_then_frege_compiler_is_successfully_downloaded_to_default_directory()
            throws Exception {
        FregeDTO minimalFregeDTO = fregeBuilder.version("'3.25.84'").release("'3.25alpha'").build();
        assertTrue(project.getTasks().getByName(SETUP_FREGE_TASK_NAME) instanceof SetupFregeTask);
        assertGradleTaskOutcome(createFregeSection(minimalFregeDTO), SETUP_FREGE_TASK_NAME);
        assertTrue(testProjectDir.toPath().resolve(Paths.get(DEFAULT_DOWNLOAD_DIRECTORY, "frege3.25.84.jar")).toFile()
                .exists());
    }

    @Test
    void given_setup_frege_compiler_task_when_frege_version_and_frege_release_and_download_directory_is_specified_then_frege_compiler_is_successfully_downloaded_to_specified_directory()
            throws Exception {
        FregeDTO fregeDTO = fregeBuilder.version("'3.25.84'").release("'3.25alpha'")
                .compilerDownloadDir("layout.projectDirectory.dir('dist')").build();
        assertTrue(project.getTasks().getByName(SETUP_FREGE_TASK_NAME) instanceof SetupFregeTask);
        assertGradleTaskOutcome(createFregeSection(fregeDTO), SETUP_FREGE_TASK_NAME);
        assertTrue(testProjectDir.toPath().resolve(Paths.get("dist", "frege3.25.84.jar")).toFile().exists());
    }

    @Test
    void frege_compile_task_is_correctly_executed() throws Exception {
        String fregeCode = "module ch.fhnw.thga.Completion where\n\n" + "complete :: Int -> (Int, String)\n"
                + "complete i = (i, \"Frege rocks\")\n";
        Files.createDirectories(testProjectDir.toPath().resolve(Paths.get("src", "main", "frege")));
        File completionFr = testProjectDir.toPath().resolve(Paths.get("src", "main", "frege", "Completion.fr"))
                .toFile();
        writeToFile(completionFr, fregeCode);
        FregeDTO minimalFregeDTO = fregeBuilder.version("'3.25.84'").release("'3.25alpha'").build();
        assertTrue(project.getTasks().getByName(COMPILE_FREGE_TASK_NAME) instanceof CompileFregeTask);
        assertGradleTaskOutcome(createFregeSection(minimalFregeDTO), COMPILE_FREGE_TASK_NAME);
        assertTrue(new File(testProjectDir.getAbsolutePath() + "/build/classes/main/frege/ch/fhnw/thga/Completion.java")
                .exists());
        assertTrue(
                new File(testProjectDir.getAbsolutePath() + "/build/classes/main/frege/ch/fhnw/thga/Completion.class")
                        .exists());
    }
}