package frege.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import frege.nativegen.*

/**
 * Created by MarkPerry on 18/02/2015.
 */
class NativeGenTask extends DefaultTask {

    String filename = "types.properties"

    String clazz = null


    @TaskAction
    void gen() {

        frege.nativegen.Main.main([clazz] as String[])
    }


}
