package frege.gradle.integtest.fixtures

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AbstractFregeIntegrationSpec extends Specification {
    public static final String DEFAULT_FREGE_VERSION = "3.23.370-g898bc8c"
    List<File> pluginClasspath

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')


        testProjectDir.newFolder("src", "main", "java", "org", "frege")
        testProjectDir.newFolder("src", "main", "frege", "org", "frege")

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


    BuildResult run(String task) {
        run(null, task);
    }

    BuildResult run(String gradleVersion, String task) {
        def writer = new StringWriter();
        GradleRunner runner = newRunner(task, writer, gradleVersion)
        def result = runner.build()
        println writer;
        return result;
    }

    BuildResult fail(String task) {
        def writer = new StringWriter();
        GradleRunner runner = newRunner(task, writer, null)
        def result = runner.buildAndFail()
        println writer;
        return result;
    }

    private GradleRunner newRunner(String task, StringWriter writer, String gradleVersion) {
        def runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(task)
                .withPluginClasspath(pluginClasspath)
                .forwardStdOutput(writer)
        if (gradleVersion) {
            runner.withGradleVersion(gradleVersion)
        }
        runner
    }

}
