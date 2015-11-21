package frege.gradle.tasks
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.process.JavaExecSpec

@TypeChecked
class FregeCompile extends AbstractCompile {

    FileCollection fregeClasspath
    FileCollection classpath

    @Input
    String stackSize = "4m"

    @Optional
    @Input
    boolean hints = false

    @Optional
    @Input
    boolean optimize = false

    @Optional
    @Input
    boolean verbose = false

    @Optional
    @Input
    boolean inline = true

    @Optional
    @Input
    boolean make = true

    @Optional
    @Input
    boolean compileGeneratedJava = true

    @Optional
    @Input
    String target = ""

    @Optional
    @Input
    boolean comments = false

    @Optional
    @Input
    boolean suppressWarnings = false

    @Optional
    @Input
    String explain = ""

    @Optional
    @Input
    String extraArgs = ""

    @Optional
    @Input
    String allArgs = "" // this is an option to overrule all other settings

    @Optional
    @Input
    String module = ""

    @Optional
    @Input
    List<File> fregePaths = []

    @Input
    String mainClass = "frege.compiler.Main"

    @Optional
    @Input
    List<String> allJvmArgs = []

    @Optional
    @Input
    String encoding = ""

    @Optional
    @Input
    String prefix = ""

    List<File> sourcePaths = []

    @Override
    @TaskAction
    protected void compile() {
        logConfigurationInfo()

        def jvmArgs = allJvmArgs
        if (jvmArgs.isEmpty()) {
            jvmArgs << "-Xss$stackSize".toString()
        }
        def compilerArgs = allArgs ? allArgs.split().toList() : assembleArguments()

        logger.info("Calling Frege compiler with compilerArgs: '$compilerArgs'")

        //TODO integrate with gradle compiler daemon infrastructure and skip internal execution
        project.javaexec(new Action<JavaExecSpec>() {
            @Override
            void execute(JavaExecSpec javaExecSpec) {
                javaExecSpec.args = compilerArgs
                javaExecSpec.classpath = FregeCompile.this.classpath + FregeCompile.this.fregeClasspath
                javaExecSpec.main = mainClass
            }
        });
    }

    public FregeCompile source(Object... sources) {
        super.source(sources);
        // track directory roots
        for (Object source : sources) {
            sourcePaths.add(project.file(source))
        }
        return this;
    }

    void logConfigurationInfo() {
        def path = project.files(compileConfig()).getAsPath()
        logger.info("Compile configuation as path: $path")
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Configuration compileConfig() {
        project.configurations.compile
    }

    protected List<String> assembleArguments() {
        List args = []
        if (hints)
            args << "-hints"
        if (optimize) {
            args << "-O"
            args << "-inline"
        }
        if (inline & !optimize)
            args << "-inline"
        if (make)
            args << "-make"
        if (!compileGeneratedJava) args << "-j"
        if (target != "") {
            args << "-target"
            args << target
        }
        if (comments) args << "-comments"
        if (suppressWarnings) args << "-nowarn"
        if (explain != "") {
            args << "-explain"
            args << explain
        }
        if (verbose)
            args << "-v"

        def fp = fregePaths
        if (!fp.isEmpty()) {
            args << "-fp"
            args << fp.collect { f -> f.absolutePath }.join(File.pathSeparator)
        }

        if (sourcePaths != null && !sourcePaths.isEmpty()) {
            args << "-sp"
            args << sourcePaths.collect { d -> d.absolutePath }.join(File.pathSeparator)
        }

        if (encoding != "") {
            args << "-enc"
            args << encoding
        }

        if (prefix != "") {
            args << "-prefix"
            args << prefix
        }

        args << "-d"
        args << getDestinationDir()

        if (!module.isEmpty()) {
            logger.info "compiling module '$module'"
            args << module
        } else {
            args = (args + extraArgs.split().toList()).toList()
        }

        args
    }

}
