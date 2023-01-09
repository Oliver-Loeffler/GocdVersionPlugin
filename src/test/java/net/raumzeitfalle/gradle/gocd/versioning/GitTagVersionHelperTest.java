package net.raumzeitfalle.gradle.gocd.versioning;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.raumzeitfalle.gradle.gocd.versioning.GitTagVersionHelper.GitDetails;

class GitTagVersionHelperTest {

    private GitTagVersionHelper classUnderTest;
    
    @Test
    void that_repository_with_tag_and_commit_history_yields_a_result() {
        classUnderTest = new GitTagVersionHelper();
        Optional<GitDetails> details = assertDoesNotThrow(()->classUnderTest.getLatestTag());
        assertTrue(details.isPresent());
        assertFalse(details.get().getTag().startsWith("refs/tags/"));
    }
    
    @Test
    void that_not_existing_does_not_cause_exceptions(@TempDir Path tempDir) {
        classUnderTest = new GitTagVersionHelper(tempDir);
        Optional<GitDetails> details = assertDoesNotThrow(()->classUnderTest.getLatestTag());
        assertFalse(details.isPresent());
    }
    

}
