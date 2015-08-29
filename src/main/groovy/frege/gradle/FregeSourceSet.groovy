package frege.gradle

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver

import static org.gradle.util.ConfigureUtil.configure

class FregeSourceSet {
	private final SourceDirectorySet frege

    FregeSourceSet(String displayName, FileResolver fileResolver) {
		frege = new DefaultSourceDirectorySet(String.format('%s Frege source', displayName), fileResolver)
		frege.filter.include('**/*.fr')
	}

	SourceDirectorySet getFrege() {
		frege
	}

    FregeSourceSet frege(Closure closure) {
		configure(closure, frege)
		this
	}
}
