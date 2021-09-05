package net.raumzeitfalle.gradle.gocd.versioning;

import groovy.lang.Closure;

import java.util.Objects;
import java.util.function.Supplier;

public class GocdVersionClosure extends Closure {

    private final Supplier<GocdEnvironment> environmentSupplier;

    private final GocdVersionPluginExtension ext;

    public GocdVersionClosure(Supplier<GocdEnvironment> environmentSupplier, GocdVersionPluginExtension ext, Object owner) {
        super(owner, owner);
        this.environmentSupplier = Objects.requireNonNull(environmentSupplier, "environmentSupplier must not be null");
        this.ext                 = Objects.requireNonNull(ext, "extension must not be null");
    }

    public GocdVersionBuilder doCall(Object autoBuildVersion, Object manualBuildVersion) {
        return new GocdVersionBuilder(environmentSupplier.get(),ext,manualBuildVersion,autoBuildVersion);
    };
}
