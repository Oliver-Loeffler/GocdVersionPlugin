package net.raumzeitfalle.gradle.gocd.versioning;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.logging.Logger;


public class GitTagVersionHelper {
    
    private final Logger logger;
    private Path workingDir;
    private Path gitDir;
    private String missingTagFallback;
    private String versionTagRegex;

    GitTagVersionHelper(Logger logger) {
        this(logger, Paths.get("."));
    }

    GitTagVersionHelper(Logger logger, Path workingDir) {
        this.workingDir = workingDir;
        this.logger = logger;
        this.gitDir = null;
        this.versionTagRegex = GitTagCollector.DEFAULT_SEMVER_TAG_REGEX;
    }

    private void logError(String errorMessage, Throwable throwable) {
        if (this.logger != null) {
            this.logger.error(errorMessage, throwable);
        }
    }
    
    private void logWarn(String warning) {
        if (this.logger != null) {
            this.logger.warn(warning);
        }
    }
    
    private void logInfo(String format, Object arg) {
        if (this.logger != null) {
            this.logger.lifecycle(format, arg);
        }
    }

    Optional<GitDetails> getLatestTag() {
        Repository repo = getRepository();
        if (null == repo) {
            return Optional.empty();
        }
        logInfo("Found .git in: {}", this.gitDir);

        String currentBranch = getBranchName(repo);
        if (null == currentBranch) {
            return Optional.empty();
        }
        
        GitDetails gitDetails = getLatestTagWithCommitCount(repo,currentBranch);
        if (!gitDetails.isValid()) {
            return Optional.empty();    
        }
        return Optional.of(gitDetails);
    }
    
    Optional<GitDetails> getLatestCommit() {
        Repository repo = getRepository();
        if (null == repo) {
            return Optional.empty();
        }
        logInfo("Found .git in: {}", this.gitDir);
        
        String currentBranch = getBranchName(repo);
        if (null == currentBranch) {
            return Optional.empty();
        }
        RevCommit sourceCommit = null;
        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commitlog = git.log().call();
            for (RevCommit commit : commitlog) {
                sourceCommit = commit;
                if (sourceCommit != null) {
                    break;
                }
            }
        } catch (Exception error) {
            logError("Could not obtain latest commit details for the given repository.", error);
        }
        
        if (sourceCommit == null) {
            return Optional.empty();
        }
                
        String alternativeTag = sourceCommit.getName().substring(0,7);
        GitDetails details = new GitDetails(alternativeTag, 0, sourceCommit);
        return Optional.of(details);
    }

    private String getTag(String refName) {
        return refName.replace(Constants.R_TAGS, "");
    }

    private GitDetails getLatestTagWithCommitCount(Repository repo, String branch) {

        GitTagCollector tagCollector = new GitTagCollector(repo, versionTagRegex, logger);
        Map<ObjectId,String> tags = tagCollector.collect();

        int commitCount = -1;
        String tagName = null;
        RevCommit lastCommit = null;
        try (Git git = new Git(repo)) {
            if (!tags.isEmpty()) {
                Map<AnyObjectId, Set<Ref>> peeledIdsByRefs = repo.getAllRefsByPeeledObjectId();
                Iterable<RevCommit> commitlog = git.log().add(repo.resolve(branch)).call();
                for (RevCommit commit : commitlog) {
                    commitCount += 1;
                    if (lastCommit == null) {
                        lastCommit = commit;
                    }

                    Set<Ref> relatedRefs = peeledIdsByRefs.get(commit.getId());
                    if (relatedRefs != null) {
                        for (Ref ref : relatedRefs) {
                            String relatedRef = tags.get(ref.getObjectId());
                            if (relatedRef != null) {
                                tagName = getTag(relatedRef);
                                break;
                            }
                        }
                    }

                    if (tagName == null) {
                        if (tags.containsKey(commit.getId())) {
                            tagName = tags.get(commit.getId())
                                          .replace(Constants.R_TAGS, "");
                            break;
                        }
                    }

                    if (tagName != null) {
                        break;
                    }
                }
            } else {
                Iterable<RevCommit> commitlog = git.log().add(repo.resolve(branch)).call();
                for (RevCommit commit : commitlog) {
                    tagName = this.missingTagFallback;
                    if (lastCommit == null) {
                        lastCommit = commit;
                    }
                    commitCount += 1;
                }
            }
        } catch (Exception error) {
            logError("Could not collect all git branch/tag/commit details for the given repository.", error);
        }
        return new GitDetails(tagName, commitCount, lastCommit);
    }
    
    

    protected String getBranchName(Repository repo) {
        try {
            return repo.getBranch();
        } catch (IOException error) {
            logError("Could not identify the active git branch!", error);
            return null;
        }
    }

    protected Repository getRepository() {
        logInfo("Looking for git repository in: {}", workingDir.toAbsolutePath().normalize());
        try {
            Repository repo = new RepositoryBuilder().findGitDir(workingDir.toFile())
                                          .readEnvironment()
                                          .setMustExist(true)
                                          .build();
            this.gitDir = repo.getDirectory().toPath().toAbsolutePath();
            return repo;
        } catch (Exception error) {
            logWarn("Could not find a valid .git repository!");
            return null;
        }
    }

    GitTagVersionHelper setMissingTagFallback(String missingTagVersionDefault) {
        Objects.requireNonNull(missingTagVersionDefault, "missing tag version default must never be null!");
        this.missingTagFallback = missingTagVersionDefault;
        return this;
    }

    GitTagVersionHelper setVersionTagRegex(String tagRegex) {
        Objects.requireNonNull(tagRegex, "Regular expression for tag selection must never be null!");
        this.versionTagRegex = tagRegex.trim();
        return this;
    }

    static class GitDetails {
        private final String tagName;
        private final int commitCount;
        private final RevCommit commit;

        public GitDetails(String tagName, int commitCount, RevCommit commit) {
            this.tagName = tagName;
            this.commitCount = commitCount;
            this.commit = commit;
        }

        boolean isValid() {
            if (commit == null) { return false; }
            if (commitCount < 0) { return false; }
            if ("".equals(tagName)) { return false; }
            return true;
        }

        public String getTag() {
            return tagName;
        }
        
        public String getShortCommitName() {
            return commit.getName().substring(0, 7);
        }
        
        public String getCommitName() {
            return commit.getName();
        }
        
        public int getCommitCount() {
            return commitCount;
        }

        public ZonedDateTime getCommitDateTime() {
            PersonIdent ident = commit.getCommitterIdent();
            TimeZone timeZone = ident.getTimeZone();
            Date date = ident.getWhen();
            return ZonedDateTime.ofInstant(date.toInstant(), timeZone.toZoneId());
        }

        public String map(GocdVersionPluginExtension extension) {
            if (extension.getAppendGitCommitCountToGitTagVersion()) {
                return tagName+"."+commitCount;
            }
            return tagName;
        }
    }
}
