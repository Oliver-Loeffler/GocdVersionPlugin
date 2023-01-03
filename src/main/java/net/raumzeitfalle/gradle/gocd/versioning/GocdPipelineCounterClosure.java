package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import groovy.lang.Closure;

@SuppressWarnings({"serial", "rawtypes"})
public class GocdPipelineCounterClosure extends Closure {
    
    private final Supplier<GocdEnvironment> environmentSupplier;

    public GocdPipelineCounterClosure(Supplier<GocdEnvironment> environmentSupplier, Object owner) {
        super(owner);
        this.environmentSupplier = environmentSupplier;
    }
    
    public int doCall() {
        return this.environmentSupplier
                   .get()
                   .getPipelineCounter();
    }
    
}
