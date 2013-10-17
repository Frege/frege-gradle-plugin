package org.gradle.frege

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.*
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction
import org.gradle.api.internal.file.FileResolver

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

    FileResolver fileResolver = getServices().get(FileResolver.class)
    JavaExecAction action = new DefaultJavaExecAction(fileResolver)
    action.setMain("frege.compiler.Main")
    action.execute
  }

}