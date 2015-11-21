package frege.gradle;

import groovy.lang.Closure;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.util.ConfigureUtil;

public class DefaultFregeSourceSet implements FregeSourceSet {
    private final SourceDirectorySet frege;
    private final SourceDirectorySet allFrege;

    public DefaultFregeSourceSet(String displayName, FileResolver fileResolver) {
        this.frege = new DefaultSourceDirectorySet(String.format("%s Frege source", new Object[]{displayName}), fileResolver);
        this.frege.getFilter().include(new String[]{"**/*.fr"});
        this.allFrege = new DefaultSourceDirectorySet(String.format("%s Frege source", new Object[]{displayName}), fileResolver);
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
