package ch.fhnw.thga.gradleplugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class FregeDTOTest {
    private final FregeDTOBuilder fregeBuilder = new FregeDTOBuilder();

    private static String buildFilePluginString(String pluginId) {
        return String.format("id '%s'", pluginId);
    }

    static String createPluginsSection(Stream<String> pluginIds) {
        String plugins = pluginIds.map(pluginId -> buildFilePluginString(pluginId)).collect(Collectors.joining("\n  "));
        return String.format("plugins {\n  %s\n}\n", plugins);
    }

    // TODO: Is there a better alternative?
    // needed helper method because reflection does NOT return the fields in order
    private void assertStringContainsSubStrings(String s, Stream<String> subStrings) {
        assertTrue(subStrings.allMatch(substring -> s.contains(substring)));
    }

    @Test
    void given_version_then_fregeDTO_can_be_converted_to_build_file_string() {
        FregeDTO fregeDTO = fregeBuilder.version("'3.25'").build();
        String expected = "version = '3.25'";
        assertEquals(expected, fregeDTO.toBuildFile());
    }

    @Test
    void given_version_and_release_then_fregeDTO_can_be_converted_to_build_file_string() {
        FregeDTO fregeDTO = fregeBuilder.version("'3.25'").release("'3.25alpha'").build();
        String expected = "version = '3.25'\n  release = '3.25alpha'";
        assertStringContainsSubStrings(expected, fregeDTO.toBuildFile().lines());
    }

    @Test
    void given_version_release_and_compiler_download_dir_then_fregeDTO_can_be_converted_to_build_file_string() {
        FregeDTO fregeDTO = fregeBuilder.version("'3.25'").release("'3.25alpha'")
                .compilerDownloadDir("layout.projectDirectory.dir('dist')").build();
        String expected = "version = '3.25'\n  release = '3.25alpha'\n  compilerDownloadDir = layout.projectDirectory.dir('dist')";
        assertStringContainsSubStrings(expected, fregeDTO.toBuildFile().lines());
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
}
