package net.raumzeitfalle.gradle.gocd.versioning;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitTagVersionHelper {
    
    private final Path workingDir;

    GitTagVersionHelper() {
        this(Paths.get("."));
    }
    
    GitTagVersionHelper(Path workingDir) {
        this.workingDir = workingDir;
    }

    Optional<GitDetails> getLatestTag() {
        Repository repo = getRepository();
        if (null == repo) {
            return Optional.empty();
        }
        
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

    private GitDetails getLatestTagWithCommitCount(Repository repo, String branch) {
        Map<ObjectId,String> tags = new HashMap<>();
        int commitCount = -1;
        String tagName = null;
        try (Git git = new Git(repo)) {
            git.tagList().call().forEach(ref->tags.put(ref.getObjectId(), ref.getName()));
            if (!tags.isEmpty()) {
                Iterable<RevCommit> commitlog = git.log().add(repo.resolve(branch)).call();
                for (RevCommit commit : commitlog) {
                    commitCount += 1;
                    if (tags.containsKey(commit.getId())) {
                        tagName = tags.get(commit.getId())
                                      .replace(Constants.R_TAGS, "");
                        break;
                    }
                }
            }
        } catch (Exception error) {
            /* failed to collect details */
        } 
        return new GitDetails(tagName, commitCount);
    }

    private String getBranchName(Repository repo) {
        try {
            return repo.getBranch();
        } catch (IOException e) {
            return null;
        }
    }

    private Repository getRepository() {
        File gitDir = new File(workingDir.toFile(), "/.git");
        try {
            return new RepositoryBuilder().findGitDir(gitDir)
                                          .readEnvironment()
                                          .build();
        } catch (Exception e) {
            return null;
        }
    }
    
    static class GitDetails {
        private final String tagName;
        private final int commitCount;

        public GitDetails(String tagName, int commitCount) {
            this.tagName = tagName;
            this.commitCount = commitCount;
        }
        
        boolean isValid() {
            return commitCount > -1 && tagName != null && !"".equals(tagName);
        }
        
        public String getTag() {
            return tagName;
        }
                
        public String map(GocdVersionPluginExtension extension) {
            if (extension.getAppendGitCommitCountToGitTagVersion()) {
                return tagName+"."+commitCount;
            }
            return tagName;
        }
    }
}
