package frege.gradle

import frege.compiler.Main
import frege.prelude.PreludeBase
import frege.runtime.Lambda
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
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

    Boolean help = false

    @Optional @Input
    String stackSize = "4m"

    @Optional @Input
    boolean hints = false

    boolean optimize = false

    @Optional @Input
    boolean verbose = false

    @Optional @Input
    boolean inline = true

    @Optional @Input
    boolean make = true

    boolean compileGeneratedJava = true

    String target = ""

    boolean comments = false

    boolean suppressWarnings = false

    String explain = ""

    @Optional @Input
    boolean skipCompile = false

    @Optional @Input
    String extraArgs = ""

    @Optional @Input
    String allArgs = "" // this is an option to overrule all other settings

    @Optional @Input
    String module = ""

    @Optional
    List<File> fregePaths = []

//    @Optional @InputDirectory
    List<File> sourcePaths = deduceSourceDir(project)

    @Optional @OutputDirectory
    File outputDir = deduceClassesDir(project)

    List<String> allJvmArgs = []

    String encoding = ""

    String prefix = ""

    // TODO: Missing presentation of types


    static File deduceSourceDir(File projectDir, String subdir) {
        new File(projectDir, subdir).exists() ?  new File(projectDir, subdir) : null
    }

    static List<File> deduceSourceDir(Project project) {
        def d = deduceSourceDir(project.projectDir, DEFAULT_SRC_DIR)
        d == null ? [] : [d]
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

    static File deduceTestSrcDir(Project project) {
        deduceSourceDir(project.projectDir, DEFAULT_TEST_SRC_DIR)
    }


    @TaskAction
    @TypeChecked(TypeCheckingMode.SKIP)
    void executeCompile() {

        if (! outputDir.exists() ) {
            logger.info "Creating output directory '${outputDir.absolutePath}'."
            outputDir.mkdirs()
        }

        // access extension configuration values as ${project.frege.key1}

        FileResolver fileResolver = getServices().get(FileResolver.class)
        JavaExecAction action = new DefaultJavaExecAction(fileResolver)
        action.setMain("frege.compiler.Main")
        def pf = project.files(project.configurations.compile)
        def path = pf.getAsPath()
        logger.info("Compile configuation as path: $path")
        action.setClasspath(project.files(project.configurations.compile))

        def args = []
        if (help) {
            args << "-help"
        } else {
            def jvmargs = allJvmArgs
            if (!allJvmArgs.isEmpty()) {
//                jvmargs << allJvmArgs
            } else if (stackSize) {
                jvmargs << "-Xss$stackSize"
            }
            action.setJvmArgs(jvmargs)
            args = allArgs ? allArgs.split().toList() : assembleArguments()
        }

        logger.info("Calling Frege compiler with args: '$args'")
        action.args(args)

        if (USE_EXTERNAl) {
            action.execute()
        } else {
            def args2 = args as String[]
//            frege.compiler.Main.main(args2)
            compile(args2)
//            frege.compiler.Main.runCompiler(args2)
        }
    }


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

    @TypeChecked(TypeCheckingMode.SKIP)
    List<File> totalFregeClasspath(List<File> fp) {
        def result = []
        result.addAll(project.files(project.configurations.compile).getFiles().toList())
        result.addAll(fp)
        result
    }

    protected List assembleArguments() {
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
            logger.info("sourcePaths1: $sourcePaths")
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

        if (!module && !extraArgs) {
            logger.info "no module and no extra args given: compiling all of the sourceDir"
            logger.info("sourcePaths2: $sourcePaths")
            if (sourcePaths != null && !sourcePaths.isEmpty()) {
                if (sourcePaths.size() != 1) {
                    throw new GradleException("No module specified and module cannot be deduced from a source path with multiple paths")
                } else {
                    args << sourcePaths.collect{d -> d.absolutePath}.join(File.pathSeparator)
                }
            }

        } else if (module) {
            logger.info "compiling module '$module'"
            args << module
        } else {
            args = (args + extraArgs.split().toList()).toList()
        }

        args
    }
}