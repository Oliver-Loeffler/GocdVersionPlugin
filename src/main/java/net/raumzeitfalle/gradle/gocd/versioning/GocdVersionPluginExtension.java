package net.raumzeitfalle.gradle.gocd.versioning;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

public class GocdVersionPluginExtension {

    private String defaultTimestampPattern = "yyyyMMddHHmmss";
    private String timestampPattern = "yyyyMMddHHmmss";
    private Supplier<LocalDateTime> timestampSupplier = ()->LocalDateTime.now();
    private boolean appendPipelineCounterToAutomatedBuilds = true;
    private boolean appendStageCounterToAutomatedBuilds = true;
    private boolean appendComputerNameToLocalBuilds = true;
    private boolean appendTimestampToLocalBuilds = true;
    private boolean appendGitCommitCountToGitTagVersion = true;
    private String  missingGitCommitFallbackTag = "<notag>";
    private String  missingGitTagVersionDefault = "0.0";
    private String  suitableTagRegex = "^\\d*([.]\\d*)?([.]\\d*)?$";

    public void setSuitableTagRegex(String newSuitableTagRegex) {
        if (newSuitableTagRegex == null || "".equals(newSuitableTagRegex.trim())) {
            this.suitableTagRegex = GitTagCollector.DEFAULT_SEMVER_TAG_REGEX;
        } else {
            this.suitableTagRegex = newSuitableTagRegex;
        }
    }

    public String getSuitableTagRegex() {
        return suitableTagRegex;
    }


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

    /**
     * Configures the plugin to the value of the GoCD pipeline stage counter to the automatically
     * generated version number for automated builds. As the pipeline stage counter does not exist for
     * local builds, this behavior only happens on GoCD build agents.
     * 
     * @param toggle if true, the counter of the current GoCD pipeline stage is appended to the
     *               automatic version number.
     */
    public void setAppendStageCounterToAutomatedBuilds(boolean toggle) {
        this.appendStageCounterToAutomatedBuilds = toggle;
    }

    public boolean getAppendComputerNameToLocalBuilds() {
        return appendComputerNameToLocalBuilds;
    }

    /**
     * Configures the plugin to append the name of the host / computer where the build is executed an
     * automatically generated version number when running outside GoCD. If the computer name cannot be
     * determined, then the suffix {@code LOCALBUILD} is appended instead.
     * 
     * @param toggle if true, the name of the computer where the build is executed is appended to the
     *               generated version number (separated by a period character).
     */
    public void setAppendComputerNameToLocalBuilds(boolean toggle) {
        this.appendComputerNameToLocalBuilds = toggle;
    }

    public boolean getAppendTimestampToLocalBuilds() {
        return appendTimestampToLocalBuilds;
    }

    /**
     * Configures the plugin to append a time stamp to an automatically generated version number of
     * local for (non-automatic) builds.
     * 
     * @param toggle if true, the generated time stamp for this particular build is appended to the
     *               generated version number (separated by a period character).
     */
    public void setAppendTimestampToLocalBuilds(boolean toggle) {
        this.appendTimestampToLocalBuilds = toggle;
    }
    
    public boolean getAppendGitCommitCountToGitTagVersion() {
        return appendGitCommitCountToGitTagVersion;
    }

    public void setAppendGitCommitCountToGitTagVersion(boolean toggle) {
        this.appendGitCommitCountToGitTagVersion = toggle;
    }
    
    public String getMissingGitCommitFallbackTag() {
        return missingGitCommitFallbackTag;
    }

    public void setMissingGitCommitFallbackTag(String fallbackTag) {
        if (fallbackTag == null) {
            this.missingGitCommitFallbackTag = "<notag>";
        } else {
            this.missingGitCommitFallbackTag = fallbackTag;
        }
    }

    public void setMissingTagVersionDefault(String defaultVersionForMissingTags) {
        Objects.requireNonNull(defaultVersionForMissingTags, "Default for missing tags must not be null!");
        this.missingGitTagVersionDefault = defaultVersionForMissingTags;
    }

    public String getMissingTagVersionDefault() {
        return missingGitTagVersionDefault;
    }
}
