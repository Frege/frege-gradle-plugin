package frege.plugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class FregePluginIntegTest extends Specification {

    public static final String DEFAULT_FREGE_VERSION = "3.23.370-g898bc8c"
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    List<File> pluginClasspath

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        buildFile << """
            plugins {
                id 'org.frege-lang'
            }

            repositories {
                jcenter()
            }
        """
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

    def "can handle non existing source directories"() {
        given:
        buildFile << """
            dependencies {
                compile "org.frege-lang:frege:$DEFAULT_FREGE_VERSION"
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('classes')
                .withPluginClasspath(pluginClasspath)
                .build()
        then:
        result.task(":compileFrege") != null
    }

    @Unroll
    def "can compile and run frege code (gradle: #gradleVersion, frege: #fregeVersion)"() {
        given:
        buildFile << """
            dependencies {
                compile "org.frege-lang:frege:$fregeVersion"
            }
            ${sayHelloTask()}
        """

        testProjectDir.newFolder("src", "main", "frege", "org", "frege")
        def fregeSourceFile = testProjectDir.newFile("src/main/frege/org/frege/HelloFrege.fr")

        fregeSourceFile << """
module org.frege.HelloFrege where

greeting = "Hello Frege!"

main _ = do
    println greeting
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(testProjectDir.root)
                .withArguments('sayHello')
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        result.output.contains("Hello Frege!")
        result.task(":sayHello").outcome == SUCCESS

        where:
        fregeVersion          | gradleVersion
        DEFAULT_FREGE_VERSION | "2.9"
        DEFAULT_FREGE_VERSION | "2.8"
        "3.22.367-g2737683"   | "2.9"
        "3.22.367-g2737683"   | "2.8"
    }


    def "can reference java from frege"() {
        given:
        buildFile << """
            dependencies {
                compile "org.frege-lang:frege:$DEFAULT_FREGE_VERSION"
            }

            ${sayHelloTask()}
        """

        and:
        testProjectDir.newFolder("src", "main", "frege", "org", "frege")

        def fregeSourceFile = testProjectDir.newFile("src/main/frege/org/frege/HelloFrege.fr")

        fregeSourceFile << """
module org.frege.HelloFrege where

data StaticHello = pure native org.frege.StaticHello where
    pure native helloJava org.frege.StaticHello.helloJava :: () -> String

main _ = do
    println(StaticHello.helloJava())

"""
        testProjectDir.newFolder("src", "main", "java", "org", "frege")
        def javaSourceFile = testProjectDir.newFile("src/main/java/org/frege/StaticHello.java")

        javaSourceFile << """
package org.frege;

public class StaticHello {
    public static String helloJava() {
        return "hello from java";
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('sayHello')
                .withPluginClasspath(pluginClasspath)
                .build()
        then:
        result.task(":compileJava").outcome == SUCCESS
        result.task(":compileFrege").outcome == SUCCESS

        result.output.contains("hello from java")
    }


    def sayHelloTask() {
        return """task sayHello(type: JavaExec){
            classpath = sourceSets.main.runtimeClasspath
            main = 'org.frege.HelloFrege'
        }"""
    }
}