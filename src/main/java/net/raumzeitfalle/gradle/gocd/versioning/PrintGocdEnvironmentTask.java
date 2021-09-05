package net.raumzeitfalle.gradle.gocd.versioning;

import org.gradle.api.tasks.TaskAction;

import java.util.Objects;
import java.util.function.Supplier;

public class PrintGocdEnvironmentTask extends org.gradle.api.DefaultTask {

    private final Supplier<GocdEnvironment> environmentSupplier;

    public PrintGocdEnvironmentTask() {
        this(()->new GocdEnvironmentImpl(System.getenv()));
    }

    PrintGocdEnvironmentTask(Supplier<GocdEnvironment> environmentSupplier) {
        this.environmentSupplier = Objects.requireNonNull(environmentSupplier);
        this.setGroup("Versioning");
        this.setDescription("Shows all Gocd related environment details");
    }

    String prepareMessage() {
        Object version = getProject().getVersion();
        GocdEnvironment env = environmentSupplier.get();
        return System.lineSeparator() + "Gocd Pipeline Environment" + System.lineSeparator() +
                "=========================" + System.lineSeparator() + System.lineSeparator() +
                "COMPUTERNAME         = " + env.getComputerName() + System.lineSeparator() +
                "GO_PIPELINE_COUNTER  = " + env.getPipelineCounter() + System.lineSeparator() +
                "Project version      = " + version + System.lineSeparator() +
                "Is automated build?  = " + env.isAutomatedBuild() + System.lineSeparator() + System.lineSeparator() +
                "";
    }

    @TaskAction
    public void printEnvironmentDetails() {
        getProject().getLogger().lifecycle(prepareMessage());
    }
}
