package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import groovy.lang.Closure;

/**
 * Attempts to determine the machine name (computer name) from the CI/CD environment.
 * The hostname can be configured to be a part of version numbers generated outside
 * a CI/CD environment.
 *
 * By default, the environment variable COMPUTERNAME is evaluated, if this does not
 * exist, the hostname is attempted to be obtained from network configuration or
 * by calling the hostname command. If in any case the hostname cannot be determined,
 * the default value here will be LOCALBUILD.
 */
@SuppressWarnings({"serial", "rawtypes"})
public class ComputerNameClosure extends Closure {
    
    private final Supplier<GocdEnvironment> environmentSupplier;

    public ComputerNameClosure(Supplier<GocdEnvironment> environmentSupplier, Object owner) {
        super(owner);
        this.environmentSupplier = environmentSupplier;
    }
    
    public String doCall() {
        return this.environmentSupplier
                   .get()
                   .getComputerName();
    }
    
}
