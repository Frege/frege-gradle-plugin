package frege.gradle.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class FregeCompileTest extends Specification {
    Project project = ProjectBuilder.builder().build()
    FregeCompile compile

    def setup() {
        when:
        compile = project.tasks.create("fregeCompile", FregeCompile)
    }


    def "configured sourcePaths tracked"() {
        when:
        compile.source("someFolder")
        then:
        compile.sourcePaths == [project.file("someFolder")]
    }


    def "default assembleArguments"() {
        given:
        compile.destinationDir = project.file("testoutput")
        expect:
        compile.assembleArguments() == ["-inline", "-make", "-d", project.file("testoutput").absolutePath]
    }

    def "with prefix"() {
        given:
        compile.destinationDir = project.file("testoutput")
        compile.prefix = "somePrefix"
        expect:
        compile.assembleArguments() == ["-inline", "-make", "-prefix", "somePrefix", "-d", project.file("testoutput").absolutePath]
    }
}
