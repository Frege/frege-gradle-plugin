package frege.gradle.plugins
import frege.gradle.integtest.fixtures.AbstractFregeIntegrationSpec
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class FregePluginIntegTest extends AbstractFregeIntegrationSpec {

    def setup() {
        buildFile << """
            plugins {
                id 'org.frege-lang'
            }

            repositories {
                jcenter()
                flatDir {
                    dirs '${new File(".").absolutePath}/lib'
                } 
            }
            compileFrege {
                classpath = files()
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
        def result = run(gradleVersion, "classes")
        then:
        result.task(":compileFrege").outcome == NO_SOURCE
        where:
        gradleVersion << ["4.0", "5.0", "5.3.1"]
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

        fregeModule()

        when:
        def result = run(gradleVersion, "sayHello")

        then:
        result.output.contains("Hello Frege!")
        result.task(":sayHello").outcome == SUCCESS

        where:
        fregeVersion          | gradleVersion
        DEFAULT_FREGE_VERSION | "5.3.1"
        DEFAULT_FREGE_VERSION | "5.0"
        DEFAULT_FREGE_VERSION | "4.0"
        "3.22.367-g2737683"   | "2.12"
    }

    private void fregeModule(String modulePath = "src/main/frege/org/frege/HelloFrege.fr") {
        def moduleFolder = new File(testProjectDir.root, modulePath).parentFile
        moduleFolder.mkdirs()
        def moduleSource = testProjectDir.newFile(modulePath)
        moduleSource << """
        module org.frege.HelloFrege where

        greeting = "Hello Frege!"

        main _ = do
            println greeting
        """
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

    def "can run frege doc on frege module"() {
        given:
        buildFile << """
        dependencies {
            compile "org.frege-lang:frege:$DEFAULT_FREGE_VERSION"
        }
        ext.destinationDir = "docs" 
        """

        and:
        fregeModule()
        when:
        BuildResult result = run("fregeDoc")
        then:
        result.task(":fregeDoc").outcome == SUCCESS
    }


    def "frege doc works with mixed sources"() {
        given:
        buildFile << """
        dependencies {
            compile "org.frege-lang:frege:$DEFAULT_FREGE_VERSION"
        }
        """

        and:
        javaCode()
        fregeCallingJava()
        when:
        BuildResult result = run("fregeDoc")
        then:
        result.task(":fregeDoc").outcome == SUCCESS
    }

    def "supports additional source sets"() {
        given:
        buildFile << """

        sourceSets {
            api
        }

        dependencies {
            apiCompile "org.frege-lang:frege:$DEFAULT_FREGE_VERSION"
        }


        """
        and:
        javaCode()
        fregeModule("src/api/frege/org/frege/HelloFrege.fr")
        when:
        BuildResult result = run("apiClasses")
        then:
        result.task(":compileApiJava").outcome == UP_TO_DATE
        result.task(":compileApiFrege").outcome == SUCCESS
        classFileExists("api/org/frege/HelloFrege.class")
    }

    def classFileExists(String relativeClasspath) {
        assert new File(testProjectDir.root, "build/classes/$relativeClasspath/").exists()
        true
    }

    def fregeCallingJava() {

        File fregeSourceFile = testProjectDir.newFile("src/main/frege/org/frege/HelloFrege.fr")
        fregeSourceFile << """
        module org.frege.HelloFrege where

        data StaticHello = pure native org.frege.java.StaticHello where
            pure native helloJava org.frege.java.StaticHello.helloJava:: () -> String


        main _ = do
            println(StaticHello.helloJava())

        """
    }

    def javaCode(String sourceRoot = "java") {
        def javaSourceFile = testProjectDir.newFile("src/main/$sourceRoot/org/frege/java/StaticHello.java")

        javaSourceFile << """
        package org.frege.java;

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