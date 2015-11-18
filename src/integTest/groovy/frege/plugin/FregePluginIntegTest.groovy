package frege.plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class FregePluginIntegTest extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    List<File> pluginClasspath

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            // try again via file reference
            pluginClasspathResource = new File("build/createClasspathManifest/plugin-classpath.txt")
            if (pluginClasspathResource == null) {
                throw new IllegalStateException("Did not find plugin classpath resource, run `integTestClasses` build task.")
            }
        }
        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def "can compile frege production code"() {
        given:
        buildFile << """
            plugins {
                id 'org.frege-lang'
            }

            repositories {
                jcenter()
            }

            dependencies {
                compile "org.frege-lang:frege:3.22.367-g2737683"
            }

            task sayHello(type: JavaExec){
                classpath = sourceSets.main.runtimeClasspath
                main = 'HelloFrege'
            }

        """

        testProjectDir.newFolder("src", "main", "frege")
        def fregeSourceFile = testProjectDir.newFile("src/main/frege/HelloFrege.fr")

        fregeSourceFile << """
module HelloFrege where

greeting = "Hello Frege!"

main _ = do
    println greeting
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('sayHello')
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        result.output.contains("Hello Frege!")
        result.task(":sayHello").outcome == SUCCESS
    }
}