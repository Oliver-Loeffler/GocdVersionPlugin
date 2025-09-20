package net.raumzeitfalle.gradle.gocd.versioning;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class GocdVersionBuilder {

    private final GocdEnvironment gocdEnvironment;

    private final String manualBuildVersion;

    private final String autoBuildVersion;

    private final GocdVersionPluginExtension extension;

    private final Logger logger;

    private final Supplier<Object> projectVersion;

    private final Path buildFilePath;

    public GocdVersionBuilder(Project project,
                              GocdEnvironment environment,
                              GocdVersionPluginExtension extension) {
        this.logger = Objects.requireNonNull(project).getLogger();
        this.gocdEnvironment = Objects.requireNonNull(environment, "environment must not be null");
        this.extension = Objects.requireNonNull(extension, "extension must not be null");
        this.projectVersion = projectVersion(project);
        this.manualBuildVersion = null;
        this.autoBuildVersion = null;
        this.buildFilePath = Objects.requireNonNull(project).getBuildFile().toPath().getParent();
    }

    public GocdVersionBuilder(Project project,
                              GocdEnvironment environment,
                              GocdVersionPluginExtension extension,
                              Object manualBuildVersion,
                              Object autoBuildVersion) {
        this.logger = Objects.requireNonNull(project).getLogger();
        this.gocdEnvironment = Objects.requireNonNull(environment, "environment must not be null");
        this.manualBuildVersion = checkProjectVersion(manualBuildVersion, "undefined");
        this.autoBuildVersion = checkProjectVersion(autoBuildVersion, this.manualBuildVersion);
        this.extension = Objects.requireNonNull(extension, "extension must not be null");
        this.projectVersion = projectVersion(project);
        this.buildFilePath = Objects.requireNonNull(project).getBuildFile().toPath().getParent();
    }

    private Supplier<Object> projectVersion(Project project) {
        return () -> {
            Object version = project.getVersion();
            if (null == version) {
                project.getLogger()
                        .debug("GocdVersionPlugin: There is no project version defined. When running gocdVersion() without arguments, ensure that a project version is specified!");
            }
            if ("unspecified".equalsIgnoreCase(String.valueOf(version).trim().toLowerCase())) {
                project.getLogger()
                        .debug("GocdVersionPlugin: The project version is unspecified. When running gocdVersion() without arguments, ensure that a project version is specified!");
            }
            return version;
        };
    }

    private String checkProjectVersion(Object versionToCheck, String defaultVersion) {
        if (null == versionToCheck || "".equalsIgnoreCase(String.valueOf(versionToCheck).trim())) {
            return defaultVersion;
        }
        return versionToCheck.toString();
    }

    public String build() {
        String auto = autoBuildVersion;
        String manual = manualBuildVersion;

        if (manualBuildVersion == null && autoBuildVersion == null) {
            String gradleProjectVersion = getProjectVersionWithFallback();
            String projectVersion = new GitTagVersionHelper(logger, buildFilePath)
                    .setVersionTagRegex(this.extension.getSuitableTagRegex())
                    .setMissingTagFallback(this.extension.getMissingTagVersionDefault())
                    .getLatestTag()
                    .map(details -> details.map(this.extension))
                    .orElseGet(() -> gradleProjectVersion);
            auto = projectVersion;
            manual = projectVersion;
        }
        return createVersion(auto, manual);
    }

    private String getProjectVersionWithFallback() {
        String gradleProjectVersion = String.valueOf(this.projectVersion.get()).trim();
        if (null == gradleProjectVersion || "unspecified".equalsIgnoreCase(gradleProjectVersion.toLowerCase())) {

            GitTagVersionHelper gitHelper = new GitTagVersionHelper(logger, buildFilePath);
            gitHelper.setVersionTagRegex(this.extension.getSuitableTagRegex());
            gitHelper.setMissingTagFallback(this.extension.getMissingTagVersionDefault());

            Optional<GitTagVersionHelper.GitDetails> gitDetails = gitHelper.getLatestCommit();
            if (gitDetails.isPresent()) {
                GitTagVersionHelper.GitDetails details = gitDetails.get();
                gradleProjectVersion = details.getShortCommitName();
            }
        }
        return gradleProjectVersion;
    }

    String createVersion(String autoVersion, String manualVersion) {
        StringBuilder versionBuilder = new StringBuilder();
        if (gocdEnvironment.isAutomatedBuild()) {
            versionBuilder.append(autoVersion);

            if (autoVersion.toUpperCase().endsWith("-SNAPSHOT")) {
                return versionBuilder.toString();
            }

            if (extension.getAppendPipelineCounterToAutomatedBuilds()) {
                versionBuilder.append(".").append(gocdEnvironment.getPipelineCounter());
            }

            if (extension.getAppendStageCounterToAutomatedBuilds()) {
                versionBuilder.append(".").append(gocdEnvironment.getStageCounter());
            }
        } else {
            String formattedTimestamp = getFormattedTimestamp();
            versionBuilder.append(manualVersion);

            if (manualVersion.toUpperCase().endsWith("-SNAPSHOT")) {
                return versionBuilder.toString();
            }

            if (extension.getAppendComputerNameToLocalBuilds()) {
                versionBuilder.append(".").append(gocdEnvironment.getComputerName());
            }
            if (extension.getAppendTimestampToLocalBuilds()) {
                versionBuilder.append(".").append(formattedTimestamp);
            }
        }
        logger.debug("GocdVersionPlugin: Version built: " + versionBuilder);
        return versionBuilder.toString();
    }

    private String getFormattedTimestamp() {
        String timestampPattern = extension.getTimestampPattern();
        LocalDateTime timestamp = extension.getTimestampSupplier().get();
        DateTimeFormatter formatter = getDateTimeFormatter(timestampPattern);
        return timestamp.format(formatter);
    }

    private DateTimeFormatter getDateTimeFormatter(String timestampPattern) {
        try {
            return DateTimeFormatter.ofPattern(timestampPattern);
        } catch (IllegalArgumentException formatError) {
            String defaultPattern = extension.getDefaultTimestampPattern();
            String template = "Failed to pares date time from String using pattern %s. Continuing with default pattern %s.";
            logger.debug(String.format(template, timestampPattern, defaultPattern));
            return DateTimeFormatter.ofPattern(defaultPattern);
        }
    }
}
