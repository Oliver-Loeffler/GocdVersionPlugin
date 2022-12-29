package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.Objects;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import groovy.lang.Closure;

@SuppressWarnings("serial")
public class JPackageVersionClosure<V> extends Closure<V> {

    private final GocdVersionPluginExtension ext;
    
    private final Logger logger;
    
    public JPackageVersionClosure(Project project, GocdVersionPluginExtension ext, Object owner) {
        super(owner);
        this.ext = Objects.requireNonNull(ext, "extension must not be null");
        this.logger = project.getLogger();
    }

    public JPackageVersionBuilder doCall(Object autoBuildVersion) {
        return new JPackageVersionBuilder(ext, autoBuildVersion, logger);
    }
}
