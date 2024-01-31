package net.raumzeitfalle.gradle.gocd.versioning;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.raumzeitfalle.gradle.gocd.versioning.GitTagVersionHelper.GitDetails;

class GitTagVersionHelperTest {

    private GitTagVersionHelper classUnderTest;

    private Path workingDir;
    
    @Test
    void that_repository_with_tag_and_commit_history_yields_a_result() {
        classUnderTest = new GitTagVersionHelper(null);
        Optional<GitDetails> details = assertDoesNotThrow(()->classUnderTest.getLatestTag());
        assertTrue(details.isPresent());
        assertFalse(details.get().getTag().startsWith("refs/tags/"));
        
        if (details.isPresent()) {
            GitDetails gitDetails = details.get();
            System.out.println(gitDetails);
        }
    }
    
    @Test
    void that_GitTagVersionHelper_traverses_upwards_and_skips_broken_git_repositoris() {
        workingDir = Paths.get(".").resolve("test_data").resolve("project").resolve("sub_project").toAbsolutePath();
        TestHelper.createDirectoryTree(workingDir);
        TestHelper.createFakeGitRepository(workingDir.resolve(".."));

        classUnderTest = new GitTagVersionHelper(null, workingDir);
        Optional<GitDetails> details = assertDoesNotThrow(()->classUnderTest.getLatestTag());
        
        assertTrue(details.isPresent());
    }

    @Test
    void that_not_existing_does_not_cause_exceptions(@TempDir Path tempDir) {
        classUnderTest = new GitTagVersionHelper(null, tempDir);
        Optional<GitDetails> details = assertDoesNotThrow(()->classUnderTest.getLatestTag());
        assertFalse(details.isPresent());
    }
    
    @Test
    void that_GitDetails_are_correct() throws Exception {
        workingDir = Paths.get(".");
        GitTagVersionHelper helper = new GitTagVersionHelper(null, workingDir);
        Repository repo = helper.getRepository();
        
        RevCommit sourceCommit = null;
        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commitlog = git.log().call();
            for (RevCommit commit : commitlog) {
                String commitName = commit.getId().getName();
                if (commitName.startsWith("c1729f1")) {
                    sourceCommit = commit;
                }
            }
        }
        assertNotNull(sourceCommit);
        

        GitDetails classUnderTest = new GitDetails("0.0.6", 17, sourceCommit);
        
        assertAll(
                ()->assertTrue(classUnderTest.isValid()),
                ()->assertEquals("0.0.6", classUnderTest.getTag()),
                ()->assertEquals("c1729f1bd98571f3b21e48388586a453428a5400", classUnderTest.getCommitName()),
                ()->assertEquals("c1729f1", classUnderTest.getShortCommitName()),
                ()->assertEquals("2023-01-10T00:05:43+01:00[GMT+01:00]", classUnderTest.getCommitDateTime().toString()),
                ()->assertEquals(17, classUnderTest.getCommitCount())
                );
    }
    

}
