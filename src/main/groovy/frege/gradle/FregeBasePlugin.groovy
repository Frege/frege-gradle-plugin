package frege.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

class FregeBasePlugin implements Plugin<Project> {
    static String EXTENSION_NAME = "frege"

    @Override
    void apply(Project project) {
        // Workaround to build proper jars on Windows, see https://github.com/Frege/frege-gradle-plugin/issues/9
        System.setProperty("file.encoding", "UTF-8")
        project.getPluginManager().apply(JavaBasePlugin.class);
        project.extensions.create(EXTENSION_NAME, FregePluginExtension)
    }
}
