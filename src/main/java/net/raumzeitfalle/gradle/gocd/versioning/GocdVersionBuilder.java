package net.raumzeitfalle.gradle.gocd.versioning;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class GocdVersionBuilder {

    private final GocdEnvironment gocdEnvironment;

    private final String manualBuildVersion;

    private final String autoBuildVersion;

    private final GocdVersionPluginExtension extension;

    public GocdVersionBuilder(GocdEnvironment environment, GocdVersionPluginExtension extension, Object manualBuildVersion, Object autoBuildVersion) {
        this.gocdEnvironment    = Objects.requireNonNull(environment, "environment must not be null");
        this.manualBuildVersion = checkProjectVersion(manualBuildVersion, "undefined");
        this.autoBuildVersion   = checkProjectVersion(autoBuildVersion, this.manualBuildVersion);
        this.extension          = Objects.requireNonNull(extension, "extension must not be null");
    }

    private String checkProjectVersion(Object versionToCheck, String defaultVersion) {
        if (null == versionToCheck || "".equalsIgnoreCase(String.valueOf(versionToCheck).trim())) {
            return defaultVersion;
        }
        return versionToCheck.toString();
    }

    public String build() {
        StringBuilder versionBuilder = new StringBuilder();
        if (gocdEnvironment.isAutomatedBuild()) {
            versionBuilder.append(autoBuildVersion);
            if(extension.getAppendPipelineCounterToAutomatedBuilds()) {
                versionBuilder.append(".").append(gocdEnvironment.getPipelineCounter());
            }
        } else {
            String formattedTimestamp = getFormattedTimestamp();
            versionBuilder.append(manualBuildVersion);
            if (extension.getAppendComputerNameToLocalBuilds()) {
                versionBuilder.append(".").append(gocdEnvironment.getComputerName());
            }
            if (extension.getAppendTimestampToLocalBuilds()) {
                versionBuilder.append(".").append(formattedTimestamp);
            }
        }
        return versionBuilder.toString();
    }

    private String getFormattedTimestamp() {
        String timestampPattern     = extension.getTimestampPattern();
        LocalDateTime timestamp     = extension.getTimestampSupplier().get();
        DateTimeFormatter formatter = getDateTimeFormatter(timestampPattern);
        return timestamp.format(formatter);
    }

    private DateTimeFormatter getDateTimeFormatter(String timestampPattern) {
        try {
            return DateTimeFormatter.ofPattern(timestampPattern);
        } catch (IllegalArgumentException formatError) {
            return DateTimeFormatter.ofPattern(extension.getDefaultTimestampPattern());
        }
    }
}
