package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.Objects;
import java.util.function.Supplier;

import org.gradle.api.Project;

import groovy.lang.Closure;

@SuppressWarnings({"serial", "rawtypes"})
public class GocdVersionClosure extends Closure {

    private final Supplier<GocdEnvironment> environmentSupplier;

    private final GocdVersionPluginExtension ext;

    private final Project project;

    public GocdVersionClosure(Project project,
                              Supplier<GocdEnvironment> environmentSupplier,
                              GocdVersionPluginExtension ext, Object owner) {
        super(owner);
        this.environmentSupplier = Objects.requireNonNull(environmentSupplier, "environmentSupplier must not be null");
        this.ext                 = Objects.requireNonNull(ext, "extension must not be null");
        this.project             = Objects.requireNonNull(project);
    }

    public GocdVersionBuilder doCall(Object autoBuildVersion, Object manualBuildVersion) {
        return new GocdVersionBuilder(project, environmentSupplier.get(),ext,manualBuildVersion,autoBuildVersion);
    }

    public GocdVersionBuilder doCall(Object version) {
        return new GocdVersionBuilder(project, environmentSupplier.get(),ext,version,version);
    }
    
    public GocdVersionBuilder doCall() {
        return new GocdVersionBuilder(project, environmentSupplier.get(),ext);
    }
}
