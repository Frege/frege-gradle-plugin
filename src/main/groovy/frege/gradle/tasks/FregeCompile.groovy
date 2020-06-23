package frege.gradle.tasks

import groovy.transform.TypeChecked
import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.process.JavaExecSpec

/* Compiler flags as of 3.25.84

-d directory    target directory for *.java and *.class files
-fp classpath   where to find imported frege packages
-enc charset    charset for source code files, standard is UTF-8
-enc DEFAULT    platform default charset for source code files
-target n.m     generate code for java version n.m, also passed to javac
-nocp           exclude java classpath from -fp
-hints          print more detailed error messages and warnings
-inline         inline functions where possible
-strict-pats    check patterns in multi-argument functions strictly from left to right
-comments       generate commented code
-explain i[-j]  print some debugging output from type checker
               regarding line(s) i (to j). May help to understand
               inexplicable type errors better.
-nowarn         don't print warnings (not recommended)
-v              verbose mode on
-make           build outdated or missing imports
-sp srcpath     look for source files in srcpath, default is .
-target x.y     generate code for java version x.y, default is the
               version of the JVM the compiler is running in.
-j              do not run the java compiler
-ascii          do not use →, ⇒, ∀ and ∷ when presenting types,
               and use ascii characters for java generics variables
-greek          make greek type variables
-fraktur        make 𝖋𝖗𝖆𝖐𝖙𝖚𝖗 type variables
-latin          make latin type variables

*/



@TypeChecked
class FregeCompile extends AbstractCompile {

    FileCollection classpath

    @Input
    String stackSize = "4m"

    @Input
    boolean hints = false

    @Input
    boolean optimize = false

    @Input
    boolean strictPats = false

    @Input
    boolean excludeJavaClasspath = false

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
    File destinationDir

    @Input
    String mainClass = "frege.compiler.Main"

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
        def jvmArgumentsToUse = allJvmArgs.empty ? ["-Xss$stackSize"] : new ArrayList<String>(allJvmArgs)
        def compilerArgs = allArgs ? allArgs.split().toList() : assembleArguments()

        logger.info("Calling Frege compiler with compilerArgs: '$compilerArgs'")
        //TODO integrate with gradle compiler daemon infrastructure and skip internal execution
        project.javaexec(new Action<JavaExecSpec>() {
            @Override
            void execute(JavaExecSpec javaExecSpec) {
                javaExecSpec.args = compilerArgs
                javaExecSpec.classpath = FregeCompile.this.classpath
                javaExecSpec.main = mainClass
                javaExecSpec.jvmArgs = jvmArgumentsToUse as List<String>
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
        if (strictPats)
            args << "-strict-pats"
        if (excludeJavaClasspath)
            args << "-nocp"
        if (make)
            args << "-make"
        if (!compileGeneratedJava)
            args << "-j"
        if (target != "") {
            args << "-target"
            args << target
        }
        if (comments)
            args << "-comments"
        if (suppressWarnings)
            args << "-nowarn"
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
