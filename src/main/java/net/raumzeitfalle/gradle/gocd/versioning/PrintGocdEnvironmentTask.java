package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gradle.api.tasks.TaskAction;

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
       
        List<String> allItems = new ArrayList<>(); 
        Arrays.stream(EnvironmentVariables.values())
              .map(EnvironmentVariables::toString)
              .forEach(allItems::add);
        
        String title = "Gocd Pipeline Environment";
        String projectVersion = "Project version";
        String isAutomatedBuild = "Is automated build?";
        allItems.add(title);
        allItems.add(projectVersion);
        allItems.add(isAutomatedBuild);
        
        int maxWidth = allItems.stream()
                               .mapToInt(String::length)
                               .max()
                               .orElse(0);
        
        List<String> allLines = new ArrayList<>();
        for (EnvironmentVariables variable : EnvironmentVariables.values()) {
            StringBuilder line = new StringBuilder(variable.toString());
            padding(line, maxWidth-variable.toString().length());
            line.append(" = ")
                .append(env.get(variable));
            allLines.add(line.toString());
        }
        
        allLines.add(padded(projectVersion, String.valueOf(version), maxWidth));
        allLines.add(padded(isAutomatedBuild, String.valueOf(env.isAutomatedBuild()), maxWidth));
        maxWidth = allLines.stream()
                           .mapToInt(String::length)
                           .max()
                           .orElse(0);
        
        StringBuilder sep = new StringBuilder();
        padding(sep, maxWidth, "-");
        
        allLines.add(allLines.size()-2, sep.toString());
        
        return new StringBuilder(sep)
                       .append(System.lineSeparator())
                       .append(title)
                       .append(System.lineSeparator())
                       .append(sep)
                       .append(System.lineSeparator())
                       .append(allLines.stream().collect(Collectors.joining(System.lineSeparator())))
                       .append(System.lineSeparator())
                       .append(sep)
                       .append(System.lineSeparator())
                       .toString();
    }
    
    String padded(String item, String value, int maxWidth) {
        StringBuilder line = new StringBuilder(item);
        padding(line, maxWidth-item.length());
        return line.append(" = ")
                   .append(value)
                   .toString();
    }

    private void padding(StringBuilder line, int width) {
        padding(line, width, " ");
    }
    private void padding(StringBuilder line, int width, String fill) {
        while (width > 0) {
            line.append(fill);
            width -= 1;
        }
    }

    @TaskAction
    public void printEnvironmentDetails() {
        getProject().getLogger().lifecycle(prepareMessage());
    }
}
