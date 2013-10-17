package org.gradle.frege

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.*

class FregeTask extends DefaultTask {

  @Input boolean hints

  @Input boolean verbose

  @Input boolean inline = true

  @Input boolean make = true

  @Input boolean skipCompile

  @Input boolean includeStale

  @TaskAction
  void executeCompile() {
    println "Compiling Frege"




  }

}