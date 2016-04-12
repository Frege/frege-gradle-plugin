package frege.gradle

import org.gradle.api.file.FileCollection

interface FregeSourceSetOutputs {
    FileCollection getDirs()
}