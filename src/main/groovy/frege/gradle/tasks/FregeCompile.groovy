package frege.gradle.tasks

import groovy.transform.TypeChecked
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.process.JavaExecSpec

@TypeChecked
class FregeCompile extends AbstractCompile {

    FileCollection classpath

    @Input
    String stackSize = "4m"

    @Input
    boolean hints = false

    @Input
    boolean optimize = false

    boolean verbose = false

    @Input
    boolean inline = true

    @Input
    boolean make = true

    @Input
    boolean compileGeneratedJava = true

    @Input
    String target = ""

    @Input
    boolean comments = false

    @Input
    boolean suppressWarnings = false

    @Input
    String explain = ""

    @Input
    String extraArgs = ""

    @Input
    String allArgs = "" // this is an option to overrule all other settings

    @Input
    String module = ""

    @Optional @InputFiles
    FileCollection fregepath

    @Input
    String mainClass = "frege.compiler.Main"

    @Optional
    @Input
    List<String> allJvmArgs = []

    @Input
    String encoding = ""

    @Input
    String prefix = ""

    List<File> sourcePaths = []

    @Override
    @TaskAction
    protected void compile() {
        def jvmArgs = allJvmArgs
        if (jvmArgs.isEmpty()) {
            jvmArgs << "-Xss$stackSize".toString()
        }
        def compilerArgs = allArgs ? allArgs.split().toList() : assembleArguments()

        logger.info("Calling Frege compiler with compilerArgs: '$compilerArgs'")

        //TODO integrate with gradle compiler daemon infrastructure and skip internal execution

        def errOutputStream = new ByteArrayOutputStream();
        def outOutputStream = new ByteArrayOutputStream();
        project.javaexec(new Action<JavaExecSpec>() {
            @Override
            void execute(JavaExecSpec javaExecSpec) {
                javaExecSpec.args = compilerArgs
                javaExecSpec.classpath = FregeCompile.this.classpath
                javaExecSpec.main = mainClass
                javaExecSpec.errorOutput = System.err;
                javaExecSpec.standardOutput = System.out;
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


        if (fregepath != null && !fregepath.isEmpty()) {
            args << "-fp"
            args << fregepath.files.collect { f -> f.absolutePath }.join(File.pathSeparator)
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
        args << getDestinationDir().absolutePath

        if (!module.isEmpty()) {
            logger.info "compiling module '$module'"
            args << module
        } else {
            args = (args + extraArgs.split().toList()).toList()
        }
        args
    }
}
