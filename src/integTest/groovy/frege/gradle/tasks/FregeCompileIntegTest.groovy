package frege.gradle.tasks
import frege.gradle.integtest.fixtures.AbstractFregeIntegrationSpec

import static org.gradle.testkit.runner.TaskOutcome.FAILED

class FregeCompileIntegTest extends AbstractFregeIntegrationSpec {

    List<File> pluginClasspath

    def setup() {
        buildFile << """
            plugins {
                id 'org.frege-lang.base'
            }

            import frege.gradle.tasks.FregeCompile

            repositories { jcenter() }

            configurations { frege {} }

            dependencies {
                frege "org.frege-lang:frege:$DEFAULT_FREGE_VERSION"
            }

            task compile(type: FregeCompile) {
                destinationDir = file("frege-output")
                source("frege-src")
                module = "frege-src"
                classpath = configurations.frege
                fregepath = configurations.frege
            }
        """

        testProjectDir.newFolder("frege-src")
    }

    def "shows compile errors"() {
        given:
        simpleFrege()
        failingFrege()
        when:
        def result = fail("compile")

        then:
        result.task(":compile").outcome == FAILED
        result.output.contains("Failing.fr:6: can't resolve `Hello`")
    }

    def failingFrege() {
        def failingFrege = testProjectDir.newFile("frege-src/Failing.fr")
        failingFrege << """

        module Failing where

        failingFun _ = do
            println(Hello)
        """
    }

    def simpleFrege() {

        def helloFrege = testProjectDir.newFile("frege-src/Hello.fr")
        helloFrege << """

        module Hello where

        import frege.prelude.PreludeBase

        main _ = do
            println("Hello From Frege")
        """
    }
}
