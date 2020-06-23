package frege.gradle.tasks
import frege.gradle.integtest.fixtures.AbstractFregeIntegrationSpec

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class FregeCompileIntegTest extends AbstractFregeIntegrationSpec {

    List<File> pluginClasspath

    def setup() {
        buildFile << """
            plugins {
                id 'org.frege-lang.base'
            }

            import frege.gradle.tasks.FregeCompile

            repositories { 
                jcenter()
                flatDir {
                    dirs '${new File(".").absolutePath}/lib'
                } 
            }

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

    def "is incremental"() {
        given:
        simpleFrege()

        buildFile << """
        compile.doLast {
            println System.identityHashCode(compile.allJvmArgs)
            println compile.allJvmArgs
            println compile.allJvmArgs.getClass()
        }
"""
        when:
        def result = run("compile")

        then:
        result.task(":compile").outcome == SUCCESS

        when:
        result = run("compile")

        then:
        result.task(":compile").outcome == UP_TO_DATE
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
