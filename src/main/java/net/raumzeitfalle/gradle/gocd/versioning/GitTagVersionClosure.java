package net.raumzeitfalle.gradle.gocd.versioning;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import groovy.lang.Closure;
import net.raumzeitfalle.gradle.gocd.versioning.GitTagVersionHelper.GitDetails;

@SuppressWarnings({"serial", "rawtypes"})
public class GitTagVersionClosure extends Closure {
    
    private final Project project;
    
    private final GocdVersionPluginExtension ext;

    public GitTagVersionClosure(Project project, 
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
        Optional<GitDetails> latestTag = gitTagHelper.getLatestTag();

        if (latestTag.isPresent()) {
            GitDetails details = latestTag.get();
            logger.lifecycle("...found Git tag: {}", details.getTag());
            return details.getTag();
        } 
        logger.warn("Cannot read latest related Git Tag to build version number. Trying latest commit ID...");

        Optional<GitDetails> latestCommit = gitTagHelper.getLatestCommit();
        if (latestCommit.isPresent()) {
            logger.lifecycle("...found commit ID: {}", latestCommit.get().getTag());
            return latestCommit.get().getTag();
        }
        logger.warn("Cannot read latest commit ID. Hence cannot build suitable version number. Using fallback tag.");

        return ext.getMissingGitCommitFallbackTag();
    }
    
}
