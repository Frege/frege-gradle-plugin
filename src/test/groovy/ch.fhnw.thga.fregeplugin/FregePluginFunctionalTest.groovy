package ch.fhnw.thga.fregeplugin

import org.junit.jupiter.api.io.TempDir;
import spock.lang.Specification

class FregePluginFunctionalTests extends Specification {
    @TempDir File testProjectDir
    File buildFile

    def setup() {
        buildFile = newFile(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'ch.fhnw.thga.frege'
            }
        """
    }
}