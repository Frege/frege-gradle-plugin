package frege.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class FregePluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()

    def setup(){
        when:
        project.plugins.apply(FregePlugin)
    }

    def "adds frege extension"(){
        expect:
        project.getExtensions().getByName(FregeBasePlugin.EXTENSION_NAME) != null
    }

    def "applies frege base plugin"() {
        expect:
        project.pluginManager.findPlugin("org.frege-lang.base") != null
    }

    def "can be identified by id"(){
        expect:
        project.pluginManager.hasPlugin("org.frege-lang")
    }
}
