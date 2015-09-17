package frege.gradle

import frege.compiler.Main
import frege.prelude.PreludeBase
import frege.runtime.Lambda
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction
import org.gradle.api.internal.file.FileResolver
import fj.data.Option

class CompileTask extends DefaultTask {

    static String DEFAULT_CLASSES_SUBDIR = "classes/main"       // TODO: should this come from a convention?
    static String DEFAULT_SRC_DIR        = "src/main/frege"     // TODO: should this come from a source set?

    static String DEFAULT_TEST_CLASSES_DIR = "classes/test"
    static String DEFAULT_TEST_SRC_DIR = "src/test/frege"

    static Boolean USE_EXTERNAl = true

    Boolean help = false

    @Optional @Input
    String xss = "4m"

    @Optional @Input
    boolean hints = false

    @Optional @Input
    boolean verbose = false

    @Optional @Input
    boolean inline = true

    @Optional @Input
    boolean make = true

    @Optional @Input
    boolean skipCompile = false

    @Optional @Input
    String extraArgs = ""

    @Optional @Input
    String allArgs = "" // this is an option to overrule all other settings

    @Optional @Input
    String module = ""

    @Optional @InputDirectory
    File sourceDir = deduceSourceDir(project)

    @Optional @OutputDirectory
    File outputDir = deduceClassesDir(project)

    @Optional
    List<String> fregePackageDirs = []

    static File deduceSourceDir(File projectDir, String subdir) {
        new File(projectDir, subdir).exists() ?  new File(projectDir, subdir) : null
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

    static File deduceTestSrcDir(Project project) {
        deduceSourceDir(project.projectDir, DEFAULT_TEST_SRC_DIR)
    }


    @TaskAction
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
            List jvmargs = []
            if (xss)
                jvmargs << "-Xss$xss"
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

    List<String> totalFregeClasspath(Project p, List<String> fp) {
        def result = []
        result.addAll(project.files(project.configurations.compile).getFiles().toList().collect { File f -> f.absolutePath })
        result.addAll(fp)
        result
    }

    protected List assembleArguments() {
        List args = []
        if (hints)
            args << "-hints"
        if (inline)
            args << "-inline"
        if (make)
            args << "-make"
        if (verbose)
            args << "-v"
        if (skipCompile)
            args << "-j"

        if (sourceDir != null) {
            args << "-sp"
            args << sourceDir.absolutePath
        }

        args << "-d"
        args << outputDir

        def fp = USE_EXTERNAl ? fregePackageDirs : totalFregeClasspath(project, fregePackageDirs)
        if (!fp.isEmpty()) {
            args << "-fp"
            args << fp.join(";")
        }

        if (!module && !extraArgs) {
            logger.info "no module and no extra args given: compiling all of the sourceDir"
            if (sourceDir != null) {
                args << sourceDir.absolutePath
            }

        } else if (module) {
            logger.info "compiling module '$module'"
            args << module
        } else {
            args = args + extraArgs.split().toList()
        }
        args
    }
}