package frege.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

public class FregeBasePluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()

    def setup(){
        when:
        project.plugins.apply(FregeBasePlugin)
    }

    def "adds frege extension"(){
        expect:
        project.getExtensions().getByName(FregeBasePlugin.EXTENSION_NAME) != null
    }

    def "applies java base plugin"(){
        expect:
        project.pluginManager.hasPlugin("java-base")
    }

    def "can be identified by id"(){
        expect:
        project.pluginManager.hasPlugin("org.frege-lang.base")
    }
}