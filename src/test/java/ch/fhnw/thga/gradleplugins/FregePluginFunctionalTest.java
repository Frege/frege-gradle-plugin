package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.FREGE_COMPILER_BUILD_FILE_KEY;
import static ch.fhnw.thga.gradleplugins.FregeExtension.FREGE_RELEASE_BUILD_FILE_KEY;
import static ch.fhnw.thga.gradleplugins.FregeExtension.FREGE_VERSION_BUILD_FILE_KEY;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_EXTENSION_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_PLUGIN_ID;
import static ch.fhnw.thga.gradleplugins.FregePlugin.SETUP_FREGE_COMPILER_TASK_NAME;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
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
    @TempDir File testProjectDir;
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
        writeFile(destination, content, true);
    }

    private static String buildFilePluginString(String pluginId) {
        return String.format("id '%s'", pluginId);
    }

    private static String buildFileFregeExtension(String fregeVersion, String fregeRelease,
            Optional<String> compilerPath) {
        String optionalCompilerPathLine = compilerPath.isPresent() ? String.format("  %s = %s\n", FREGE_COMPILER_BUILD_FILE_KEY, compilerPath.get()) : "";
        return String.format("%s {\n  %s = %s\n  %s = %s\n%s}\n", FREGE_EXTENSION_NAME, FREGE_VERSION_BUILD_FILE_KEY,
                fregeVersion, FREGE_RELEASE_BUILD_FILE_KEY, fregeRelease, optionalCompilerPathLine);
    }

    private static String writeBuildFilePlugins(Stream<String> pluginIds) {
        String plugins = pluginIds.map(pluginId -> buildFilePluginString(pluginId)).collect(Collectors.joining("\n  "));
        return String.format("plugins {\n  %s\n}\n", plugins);
    }

    private void assertSetupFregeCompilerTask(String fregeConfig) throws Exception {
        System.out.println(fregeConfig);
        appendToFile(buildFile, fregeConfig);
        assertTrue(project.getTasks().getByName(SETUP_FREGE_COMPILER_TASK_NAME) instanceof SetupFregeCompilerTask);
        BuildResult result = GradleRunner.create().withProjectDir(testProjectDir).withPluginClasspath()
                .withArguments(SETUP_FREGE_COMPILER_TASK_NAME).build();
        assertEquals(SUCCESS, result.task(":" + SETUP_FREGE_COMPILER_TASK_NAME).getOutcome());
    }

    @BeforeEach
    void setup() throws Exception {
        buildFile = new File(testProjectDir, "build.gradle");
        settingsFile = new File(testProjectDir, "settings.gradle");
        writeToFile(buildFile, writeBuildFilePlugins(Stream.of(FREGE_PLUGIN_ID)));
        writeToFile(settingsFile, "rootProject.name='frege-plugin'");
        project = ProjectBuilder.builder().withProjectDir(testProjectDir).build();
        project.getPluginManager().apply(FREGE_PLUGIN_ID);
    }

    @Test
    void given_single_plugin_id_then_it_is_correctly_converted_to_build_file_string() {
        String pluginId = "frege";
        Stream<String> pluginIds = Stream.of(pluginId);
        String expected = "plugins {\n" + "  id '" + pluginId + "'\n" + "}\n";
        assertEquals(expected, writeBuildFilePlugins(pluginIds));
    }

    @Test
    void given_multiple_plugin_ids_then_they_are_correctly_converted_to_build_file_string() {
        String fregeId = "frege";
        String javaId = "java";
        Stream<String> pluginIds = Stream.of(fregeId, javaId);
        String expected = "plugins {\n" + "  id '" + fregeId + "'\n" + "  id '" + javaId + "'\n" + "}\n";
        assertEquals(expected, writeBuildFilePlugins(pluginIds));
    }

    @Test
    void given_default_plugin_ids_then_they_are_correctly_converted_to_build_file_string() {
        String fregeId = "frege";
        String javaId = "java";
        Stream<String> pluginIds = Stream.of(fregeId, javaId);
        String expected = "plugins {\n" + "  id '" + fregeId + "'\n" + "  id '" + javaId + "'\n" + "}\n";
        assertEquals(expected, writeBuildFilePlugins(pluginIds));
    }

    @Test
    void given_frege_version_and_frege_release_then_they_are_correctly_converted_to_build_file_string() {
        String fregeVersion = "'3.25.84'";
        String fregeRelease = "'3.25alpha'";
        String expectedFregeConfig = FREGE_EXTENSION_NAME + " {\n" + "  fregeVersion = " + fregeVersion + "\n"
                + "  fregeRelease = " + fregeRelease + "\n" + "}\n";
        assertEquals(expectedFregeConfig, buildFileFregeExtension(fregeVersion, fregeRelease, Optional.empty()));
    }

    @Test
    void given_frege_version_and_frege_release_and_frege_compiler_path_then_they_are_correctly_converted_to_build_file_string() {
        String fregeVersion = "'3.25.84'";
        String fregeRelease = "'3.25alpha'";
        String fregeCompilerPath = "layout.projectDirectory.file('lib/frege3.25.84')";
        String expectedFregeConfig =
            FREGE_EXTENSION_NAME + " {\n" + "  fregeVersion = " + fregeVersion + "\n"
        + "  fregeRelease = " + fregeRelease + "\n"
        + "  fregeCompilerPath = " + fregeCompilerPath + "\n" + "}\n";
        assertEquals(expectedFregeConfig, buildFileFregeExtension(fregeVersion, fregeRelease, Optional.of(fregeCompilerPath)));
    }

    @Test
    void given_frege_compiler_version_3_25_84_and_3_25_alpha_release_without_frege_compiler_path_when_running_the_setup_frege_compiler_task_then_the_frege_compiler_is_correctly_setup()
            throws Exception {
        String fregeConfig = buildFileFregeExtension("'3.25.84'", "'3.25alpha'", Optional.empty());
        assertSetupFregeCompilerTask(fregeConfig);
    }

    @Test
    void given_frege_compiler_version_3_25_84_when_running_the_setup_frege_compiler_task_then_the_frege_compiler_is_correctly_setup()
            throws Exception {
        String fregeConfig = buildFileFregeExtension("'3.25.84'", "'3.25alpha'", Optional.of("layout.projectDirectory.file('lib/frege3.25.84')"));
        assertSetupFregeCompilerTask(fregeConfig);
    }
}