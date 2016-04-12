package frege.gradle

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternFilterable

interface FregeSourceDirectorySet extends PatternFilterable {
    def String getName()

    def FregeSourceDirectorySet srcDir(Object srcPath)

    def FregeSourceDirectorySet srcDirs(Object... srcPaths)

    def Set<File> getSrcDirs()

    def FregeSourceDirectorySet setSrcDirs(Iterable<?> srcPaths)

    def FileTree getFiles()

    def PatternFilterable getFilter()

    def FregeSourceSetOutputs getOutput()

    def String getGeneratorTaskName()

    boolean contains(File file)
}