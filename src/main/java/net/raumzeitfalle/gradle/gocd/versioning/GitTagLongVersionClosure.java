package net.raumzeitfalle.gradle.gocd.versioning;

import groovy.lang.Closure;
import net.raumzeitfalle.gradle.gocd.versioning.GitTagVersionHelper.GitDetails;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings({"serial", "rawtypes"})
public class GitTagLongVersionClosure extends Closure {

    private final Project project;

    private final GocdVersionPluginExtension ext;

    public GitTagLongVersionClosure(Project project,
                                    GocdVersionPluginExtension ext,
                                    Object owner) {
        super(owner);
        this.project = Objects.requireNonNull(project);
        this.ext     = Objects.requireNonNull(ext, "extension must not be null");
    }
    
    public String doCall() {
        Logger logger = this.project.getLogger();
        Path buildFilePath = this.project.getBuildFile().toPath().getParent();
        logger.lifecycle("Build file path: {}", buildFilePath);
        GitTagVersionHelper gitTagHelper = new GitTagVersionHelper(logger, buildFilePath);

        gitTagHelper.setMissingTagFallback(this.ext.getMissingTagVersionDefault());
        gitTagHelper.setVersionTagRegex(this.ext.getSuitableTagRegex());

        Optional<GitDetails> latestTag = gitTagHelper.getLatestTag();

        StringBuilder versionBuilder = new StringBuilder();

        if (latestTag.isPresent()) {
            GitDetails details = latestTag.get();
            logger.lifecycle("...found Git tag: {}", details.getTag());
            versionBuilder.append(details.getTag())
                    .append(".")
                    .append(details.getCommitCount())
                    .append("-")
                    .append(details.getShortCommitName());
            return versionBuilder.toString();
        }

        logger.warn("Cannot read latest related Git Tag to build version number. Trying latest commit ID...");

        Optional<GitDetails> latestCommit = gitTagHelper.getLatestCommit();
        if (latestCommit.isPresent()) {
            logger.lifecycle("...found commit ID: {}", latestCommit.get().getTag());
            GitDetails details = latestCommit.get();
            versionBuilder.append(details.getShortCommitName())
                    .append(".")
                    .append(details.getCommitCount());
            return versionBuilder.toString();
        }
        logger.warn("Cannot read latest commit ID. Hence cannot build suitable version number. Using fallback tag.");
        return ext.getMissingGitCommitFallbackTag().toString();
    }

}
