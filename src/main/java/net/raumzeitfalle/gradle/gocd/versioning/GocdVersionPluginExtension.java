package net.raumzeitfalle.gradle.gocd.versioning;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.Supplier;

public class GocdVersionPluginExtension {

    private String defaultTimestampPattern = "yyyyMMddHHmmss";
    private String timestampPattern = defaultTimestampPattern;
    private Supplier<LocalDateTime> timestampSupplier = ()->LocalDateTime.now();
    private boolean appendPipelineCounterToAutomatedBuilds = true;
    private boolean appendComputerNameToLocalBuilds = true;
    private boolean appendTimestampToLocalBuilds = true;

    public Supplier<LocalDateTime> getTimestampSupplier() {
        return timestampSupplier;
    }

    public void setTimestampSupplier(Supplier<LocalDateTime> timestampSupplier) {
        if (timestampSupplier == null) {
            this.timestampSupplier = ()->LocalDateTime.now();
        } else {
            this.timestampSupplier = timestampSupplier;
        }
    }

    public void setTimestampPattern(String timestampPattern) {
        if (timestampPattern == null || timestampPattern.isBlank()) {
            this.timestampPattern = defaultTimestampPattern;
        } else {
            this.timestampPattern = timestampPattern;
        }
    }

    public String getTimestampPattern() {
        return timestampPattern;
    }

    protected String getDefaultTimestampPattern() {
        return defaultTimestampPattern;
    }

    public boolean getAppendPipelineCounterToAutomatedBuilds() {
        return appendPipelineCounterToAutomatedBuilds;
    }

    public void setAppendPipelineCounterToAutomatedBuilds(boolean toggle) {
        this.appendPipelineCounterToAutomatedBuilds = toggle;
    }

    public boolean getAppendComputerNameToLocalBuilds() {
        return appendComputerNameToLocalBuilds;
    }

    public void setAppendComputerNameToLocalBuilds(boolean toggle) {
        this.appendComputerNameToLocalBuilds = toggle;
    }

    public boolean getAppendTimestampToLocalBuilds() {
        return appendTimestampToLocalBuilds;
    }

    public void setAppendTimestampToLocalBuilds(boolean toggle) {
        this.appendTimestampToLocalBuilds = toggle;
    }
}
