package frege.gradle.tasks

import org.apache.commons.io.output.TeeOutputStream
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

class FregeDoc extends DefaultTask {

    /* Usage: java -jar fregec.jar frege.tools.Doc [-v] [-d opt] [-x mod,...] modules ...
     * -v              print a message for each processed module
     * -d docdir       specify root directory for documentation
     *                 Documentation for module x.y.Z will be writen to
     *                 $docdir/x/y/Z.html
     * -cp classpath   class path for doc tool
     * -x mod1[,mod2]  exclude modules whose name starts with 'mod1' or 'mod2'
     *
     * Modules can be specified in three ways:
     *  my.nice.Modul   by name, the Java class for this module must be on the class path
     *  directory/      all modules that could be loaded if the given directory was on the class path, except exxcluded ones
     *  path.jar        all modules in the specified JAR file, except excluded ones
     *
     * Example: document base frege distribution without compiler modules
     *      java -cp fregec.jar frege.tools.Doc -d doc -x frege.compiler fregec.jar
     *
     */

    static String DEFAULT_DOCS_SUBDIR = "docs/frege"       // TODO: should this come from a convention?

    @Optional
    @OutputDirectory
    File targetDir = new File(project.buildDir, DEFAULT_DOCS_SUBDIR)

    @Input
    String module // module name or directory or class path. Default is all production modules

    @Input
    @Optional
    String exclude = null

    @Input
    @Optional
    Boolean verbose = null

    FileCollection classpath

    @TaskAction
    void fregedoc() {
        ByteArrayOutputStream berr = new ByteArrayOutputStream()
        def teeOutputStream = new TeeOutputStream(System.err, berr)
        def result = project.javaexec(new Action<JavaExecSpec>() {
            @Override
            void execute(JavaExecSpec javaExecSpec) {
                if (verbose) {
                    javaExecSpec.args '-v'
                }
                javaExecSpec.args '-d', targetDir.absolutePath
                if (exclude) {
                    javaExecSpec.args '-x', exclude
                }
                javaExecSpec.args(module)
                javaExecSpec.main = "frege.tools.Doc"
                javaExecSpec.workingDir = project.projectDir
                javaExecSpec.standardInput = System.in
                javaExecSpec.standardOutput = System.out
                javaExecSpec.errorOutput = teeOutputStream
                javaExecSpec.classpath = this.classpath

                javaExecSpec.ignoreExitValue = true
            }
        })

        //Workaround for failing with java sources. should result in exit value 0 anyway.
        def berrString = berr.toString()
        if (result.exitValue !=0 && !berrString.contains("there were errors for")) {
            throw new GradleException("Non zero exit value running FregeDoc.");
        }
    }
}

