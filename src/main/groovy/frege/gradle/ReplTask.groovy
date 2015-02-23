package frege.gradle

import frege.repl.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by MarkPerry on 17/02/2015.
 */
public class ReplTask extends DefaultTask {

    @TaskAction
    void repl() {
        frege.repl.FregeRepl.main([] as String[])
    }
}
