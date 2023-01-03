package net.raumzeitfalle.gradle.gocd.versioning;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class GocdVersionPluginExtension {

    private String defaultTimestampPattern = "yyyyMMddHHmmss";
    private String timestampPattern = "yyyyMMddHHmmss";
    private Supplier<LocalDateTime> timestampSupplier = ()->LocalDateTime.now();
    private boolean appendPipelineCounterToAutomatedBuilds = true;
    private boolean appendStageCounterToAutomatedBuilds = true;
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
        if (timestampPattern == null || "".equalsIgnoreCase(timestampPattern.trim())) {
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
    
    public boolean getAppendStageCounterToAutomatedBuilds() {
        return appendStageCounterToAutomatedBuilds;
    }

    public void setAppendStageCounterToAutomatedBuilds(boolean toggle) {
        this.appendStageCounterToAutomatedBuilds = toggle;
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
