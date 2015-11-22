package frege.gradle.plugins
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class FregePluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()

    def setup(){
        when:
        project.plugins.apply(FregePlugin)
    }

    def "applies frege base plugin"() {
        expect:
        project.pluginManager.findPlugin("org.frege-lang.base") != null
    }

    def "can be identified by id"(){
        expect:
        project.pluginManager.hasPlugin("org.frege-lang")
    }

    @Unroll
    def "adds #fregeTaskName task"(){
        when:
        def fregeTask = project.tasks.findByName(fregeTaskName)
        then:
        fregeTask != null
        fregeTask.group == "frege"
        where:
        fregeTaskName << ["fregeRepl", "fregeDoc", "fregeQuickCheck", "fregeNativeGen"]
    }
}
