package frege.gradle.plugins

import frege.gradle.integtest.fixtures.AbstractFregeIntegrationSpec
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll
import static org.gradle.testkit.runner.TaskOutcome.*

class FregePluginIntegTest extends AbstractFregeIntegrationSpec {

    def setup() {
        buildFile << """
            plugins {
                id 'org.frege-lang'
            }

            repositories {
                jcenter()
            }
        """
    }

    def "can handle non existing source directories"() {
        given:
        buildFile << """
        dependencies {
            compile "org.frege-lang:frege:$DEFAULT_FREGE_VERSION"
        }
        """

        when:
        def result = run("classes")
        then:
        result.task(":compileFrege").outcome == UP_TO_DATE
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

        def fregeSourceFile = testProjectDir.newFile("src/main/frege/org/frege/HelloFrege.fr")

        fregeSourceFile << """
        module org.frege.HelloFrege where

        greeting = "Hello Frege!"

        main _ = do
            println greeting
        """

        when:
        def result = run(gradleVersion, "sayHello")

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
        javaCode()
        fregeCallingJava()
        when:
        BuildResult result = run("sayHello")
        then:
        result.task(":compileJava").outcome == SUCCESS
        result.task(":compileFrege").outcome == SUCCESS
        result.output.contains("hello from java")
    }

    def fregeCallingJava() {

        File fregeSourceFile = testProjectDir.newFile("src/main/frege/org/frege/HelloFrege.fr")
        fregeSourceFile << """
        module org.frege.HelloFrege where

        data StaticHello = pure native org.frege.StaticHello where
            pure native helloJava org.frege.StaticHello.helloJava:: () -> String


        main _ = do
            println(StaticHello.helloJava())

        """
    }

    def javaCode(String sourceRoot = "java") {
        def javaSourceFile = testProjectDir.newFile("src/main/$sourceRoot/org/frege/StaticHello.java")

        javaSourceFile << """
        package org.frege;

        public class StaticHello {
            public static String helloJava() {
                return "hello from java";
            }
        }
        """
    }

    def sayHelloTask() {
        return """ task sayHello(type: JavaExec) {
            classpath = sourceSets.main.runtimeClasspath
            main = 'org.frege.HelloFrege'
        } """
    }
}