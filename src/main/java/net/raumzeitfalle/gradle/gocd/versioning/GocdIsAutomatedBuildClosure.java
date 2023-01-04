package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import groovy.lang.Closure;

@SuppressWarnings({"serial", "rawtypes"})
public class GocdIsAutomatedBuildClosure extends Closure {
    
    private final Supplier<GocdEnvironment> environmentSupplier;

    public GocdIsAutomatedBuildClosure(Supplier<GocdEnvironment> environmentSupplier, Object owner) {
        super(owner);
        this.environmentSupplier = environmentSupplier;
    }
    
    public boolean doCall() {
        return this.environmentSupplier
                   .get()
                   .isAutomatedBuild();
    }
    
}
