package net.raumzeitfalle.gradle.gocd.versioning;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.logging.Logger;

public class GitTagVersionHelper {
    
    private final Logger logger;
    
    private Path workingDir;
    
    GitTagVersionHelper(Logger logger) {
        this(logger, Paths.get("."));
    }
    
    GitTagVersionHelper(Logger logger, Path workingDir) {
        this.workingDir = workingDir;
        this.logger = logger;
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
        logInfo("Found .git in: {}", this.workingDir.toAbsolutePath().normalize());

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
        logInfo("Found .git in: {}", this.workingDir.toAbsolutePath().normalize());
        
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
    
    private GitDetails getLatestTagWithCommitCount(Repository repo, String branch) {
        Map<ObjectId,String> tags = new HashMap<>();
        int commitCount = -1;
        String tagName = null;
        RevCommit lastCommit = null;
        try (Git git = new Git(repo)) {
            List<Ref> refs = repo.getRefDatabase().getRefsByPrefix(Constants.R_TAGS);
            refs.forEach(ref->tags.put(ref.getObjectId(), ref.getName()));
            if (!tags.isEmpty()) {
                Iterable<RevCommit> commitlog = git.log().add(repo.resolve(branch)).call();
                commitCount = 0;
                for (RevCommit commit : commitlog) {
                    commitCount += 1;
                    if (lastCommit == null) {
                        lastCommit = commit;
                    }
                    if (tags.containsKey(commit.getId())) {
                        tagName = tags.get(commit.getId())
                                      .replace(Constants.R_TAGS, "");
                        break;
                    }
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
                                          .build();
            return repo;
        } catch (Exception error) {
            logWarn("Could not find a valid .git repository!");
            return null;
        }
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
