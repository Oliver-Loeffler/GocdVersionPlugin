package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.Objects;

import groovy.lang.Closure;

@SuppressWarnings("serial")
public class JPackageVersionClosure<V> extends Closure<V> {

    private final GocdVersionPluginExtension ext;
    
    public JPackageVersionClosure(GocdVersionPluginExtension ext, Object owner) {
        super(owner);
        this.ext = Objects.requireNonNull(ext, "extension must not be null");
    }

    public JPackageVersionBuilder doCall(Object autoBuildVersion) {
        return new JPackageVersionBuilder(ext, autoBuildVersion);
    }
}
