package frege.gradle;

import groovy.lang.Closure;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.util.ConfigureUtil;

public class DefaultFregeSourceSet implements FregeSourceSet {
    private final SourceDirectorySet frege;
    private final SourceDirectorySet allFrege;

    public DefaultFregeSourceSet(String displayName, FregeSourceSetDirectoryFactory sourceSetFactory) {
        this.frege = sourceSetFactory.newSourceSetDirectory(String.format("%s Frege source", new Object[]{displayName}));
        this.frege.getFilter().include(new String[]{"**/*.fr"});
        this.allFrege = sourceSetFactory.newSourceSetDirectory(String.format("%s Frege source", new Object[]{displayName}));
        this.allFrege.source(this.frege);
        this.allFrege.getFilter().include(new String[]{"**/*.fr"});
    }

    public SourceDirectorySet getFrege() {
        return this.frege;
    }

    public FregeSourceSet frege(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, this.getFrege());
        return this;
    }

    public SourceDirectorySet getAllFrege() {
        return this.allFrege;
    }
}
