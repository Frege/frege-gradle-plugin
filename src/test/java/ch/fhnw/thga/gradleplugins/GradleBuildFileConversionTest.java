package ch.fhnw.thga.gradleplugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class GradleBuildFileConversionTest {
    private static FregeDTOBuilder fregeBuilder;

    private static String buildFilePluginString(String pluginId) {
        return String.format("id '%s'", pluginId);
    }

    static String createPluginsSection(Stream<String> pluginIds) {
        String plugins = pluginIds.map(pluginId -> buildFilePluginString(pluginId)).collect(Collectors.joining("\n  "));
        return String.format("plugins {\n  %s\n}\n", plugins);
    }

    private void assertStringContainsSubStrings(String s, Stream<String> subStrings) {
        assertTrue(subStrings.allMatch(substring -> s.contains(substring)));
    }

    @BeforeEach
    void setup() {
        fregeBuilder = FregeDTOBuilder.getInstance();
    }

    @Nested
    @IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Converting_Frege_DTO_to_build_file_key_value_pairs_works {

        @Test
        void given_single_version_property() {
            FregeDTO fregeDTO = fregeBuilder.version("'3.25'").build();
            String expected = "version = '3.25'";
            assertEquals(expected, fregeDTO.toBuildFile());
        }

        @Test
        void given_multiple_properties_in_non_deterministic_order() {
            FregeDTO fregeDTO = fregeBuilder.version("'3.25'").release("'3.25alpha'")
                    .compilerDownloadDir("layout.projectDirectory.dir('dist')").build();
            String expected = "version = '3.25'\n  release = '3.25alpha'\n  compilerDownloadDir = layout.projectDirectory.dir('dist')";
            assertStringContainsSubStrings(expected, fregeDTO.toBuildFile().lines());
        }
    }

    @Nested
    @IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Creating_plugin_section_works {

        @Test
        void given_single_plugin_id() {
            String pluginId = "frege";
            Stream<String> pluginIds = Stream.of(pluginId);
            String expected = "plugins {\n" + "  id '" + pluginId + "'\n" + "}\n";
            assertEquals(expected, createPluginsSection(pluginIds));
        }

        @Test
        void given_multiple_plugin_ids() {
            String fregeId = "frege";
            String javaId = "java";
            Stream<String> pluginIds = Stream.of(fregeId, javaId);
            String expected = "plugins {\n" + "  id '" + fregeId + "'\n" + "  id '" + javaId + "'\n" + "}\n";
            assertEquals(expected, createPluginsSection(pluginIds));
        }
    }
}
