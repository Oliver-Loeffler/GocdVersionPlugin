package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import groovy.lang.Closure;

@SuppressWarnings({"serial", "rawtypes"})
public class GocdEnvironmentNameClosure extends Closure {
    
    private final Supplier<GocdEnvironment> environmentSupplier;

    public GocdEnvironmentNameClosure(Supplier<GocdEnvironment> environmentSupplier, Object owner) {
        super(owner);
        this.environmentSupplier = environmentSupplier;
    }
    
    public String doCall() {
        return this.environmentSupplier
                   .get()
                   .getEnvironmentName();
    }
    
}
