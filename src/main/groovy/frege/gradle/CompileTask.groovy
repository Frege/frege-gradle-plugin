package frege.gradle

import frege.compiler.Main
import frege.prelude.PreludeBase
import frege.runtime.Lambda
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction
import org.gradle.api.internal.file.FileResolver

@TypeChecked
class CompileTask extends DefaultTask {

    // see help at https://github.com/Frege/frege/wiki/Compiler-Manpage

    static String DEFAULT_CLASSES_SUBDIR = "classes/main"       // TODO: should this come from a convention?
    static String DEFAULT_SRC_DIR        = "src/main/frege"     // TODO: should this come from a source set?

    static String DEFAULT_TEST_CLASSES_DIR = "classes/test"
    static String DEFAULT_TEST_SRC_DIR = "src/test/frege"

    static Boolean USE_EXTERNAl = true

    @Optional @Input
    boolean enabled = true

    @Optional @Input
    Boolean help = false

    @Optional @Input
    String stackSize = "4m"

    @Optional @Input
    boolean hints = false

    @Optional @Input
    boolean optimize = false

    @Optional @Input
    boolean verbose = false

    @Optional @Input
    boolean inline = true

    @Optional @Input
    boolean make = true

    @Optional @Input
    boolean compileGeneratedJava = true

    @Optional @Input
    String target = ""

    @Optional @Input
    boolean comments = false

    @Optional @Input
    boolean suppressWarnings = false

    @Optional @Input
    String explain = ""

    @Optional @Input
    boolean skipCompile = false

    @Optional @Input
    String extraArgs = ""

    @Optional @Input
    String allArgs = "" // this is an option to overrule all other settings

    @Optional @Input
    String module = ""

    @Optional @Input
    List<File> fregePaths = []

    @InputFiles
    List<File> sourcePaths = [deduceSourceDir(project)]

    @Optional @OutputDirectory
    File outputDir = deduceClassesDir(project)

    @Optional @Input
    String mainClass = "frege.compiler.Main"

    @Optional @Input
    List<String> allJvmArgs = []

    @Optional @Input
    String encoding = ""

    @Optional @Input
    String prefix = ""

    // TODO: Missing presentation of types (ascii, symbols, latin, greek, faktur)

    static File deduceSourceDir(File projectDir, String subdir) {
        new File(projectDir, subdir)
    }

    static File deduceSourceDir(Project project) {
        deduceSourceDir(project.projectDir, DEFAULT_SRC_DIR)
    }

    static File deduceClassesDir(File projectDir, String subdir) {
        new File(projectDir, subdir)
    }

    static File deduceClassesDir(Project project) {
        deduceClassesDir(project.buildDir, DEFAULT_CLASSES_SUBDIR)
    }

    static File deduceTestClassesDir(Project project) {
        deduceClassesDir(project.buildDir, DEFAULT_TEST_CLASSES_DIR)
    }

    static File deduceTestSourceDir(Project project) {
        deduceSourceDir(project.projectDir, DEFAULT_TEST_SRC_DIR)
    }

    @TaskAction
    void executeCompile() {

        if (!enabled) {
            logger.info("Frege compiler disabled.")
            return;
        }

        if (!outputDir.exists() ) {
            logger.info "Creating output directory '${outputDir.absolutePath}'."
            outputDir.mkdirs()
        }
        // access extension configuration values as ${project.frege.key1}

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain(mainClass)

        logConfigurationInfo()

        action.setClasspath(actionClasspath(project))

        def args = []
        if (help) {
            args << "-help"
        } else {
            def jvmArgs = allJvmArgs
            if (jvmArgs.isEmpty()) {
                jvmArgs << "-Xss$stackSize".toString()
            }
            action.setJvmArgs(jvmArgs)
            args = allArgs ? allArgs.split().toList() : assembleArguments()
        }

        logger.info("Calling Frege compiler with args: '$args'")
        action.args(args)

        if (USE_EXTERNAl) {
            action.execute()
        } else {
            compile(args as String[])
        }
    }

    void logConfigurationInfo() {
        def path = project.files(compileConfig()).getAsPath()
        logger.info("Compile configuation as path: $path")

    }

    FileCollection actionClasspath(Project p) {
        p.files(compileConfig()) + p.files(deduceClassesDir(p))
    }

    // TODO: This should be removed or integrated so an external Java process does not need to be started.
    // The Java generated uses System.exit() which does not work well with Gradle
    // This was taken from the frege fork /compiler1/build/classes/main/afrege/compiler/Main.java:main()
    void compile(String[] paramArrayOfString) {
        long l1 = System.nanoTime();
        Integer localInteger = frege.runtime.Runtime.runMain(
            PreludeBase.TST.performUnsafe(
                (Lambda)Main.Ĳ._mainƒd0fa0028.inst.apply(PreludeBase._toList(paramArrayOfString)).forced()
            )
        );
        long l2 = System.nanoTime();
        ((PrintWriter)frege.runtime.Runtime.stderr.get()).println("runtime " + (l2 - l1 + 500000L) / 1000000L / 1000.0D + " wallclock seconds.");
        if (localInteger != null) {
//            System.exit(localInteger.intValue());
        }
    }

    List<File> totalFregeClasspath(List<File> fp) {
        def result = []
        result.addAll(project.files(compileConfig()).getFiles().toList())
        result.addAll(fp)
        result
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
        if (skipCompile)
            args << "-j"

        def fp = USE_EXTERNAl ? fregePaths : totalFregeClasspath(fregePaths)
        if (!fp.isEmpty()) {
            args << "-fp"
            args << fp.collect{f -> f.absolutePath}.join(File.pathSeparator)
        }

        if (sourcePaths != null && !sourcePaths.isEmpty()) {
            args << "-sp"
            args << sourcePaths.collect{d -> d.absolutePath}.join(File.pathSeparator)
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
        args << outputDir

        if (!module.isEmpty()) {
            logger.info "compiling module '$module'"
            args << module
        } else {
            args = (args + extraArgs.split().toList()).toList()
        }

        args
    }

}
