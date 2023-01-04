package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import groovy.lang.Closure;

@SuppressWarnings({"serial", "rawtypes"})
public class GocdMaterialBranchNameClosure extends Closure {
    
    private final Supplier<GocdEnvironment> environmentSupplier;

    public GocdMaterialBranchNameClosure(Supplier<GocdEnvironment> environmentSupplier, Object owner) {
        super(owner);
        this.environmentSupplier = environmentSupplier;
    }
    
    public String doCall(String materialName) {
        return this.environmentSupplier
                   .get()
                   .getMaterialBranch(materialName);
    }
    
}
