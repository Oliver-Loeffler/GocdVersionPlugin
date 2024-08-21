package net.raumzeitfalle.gradle.gocd.versioning;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class GitTagCollectorTest {

    private GitTagCollector classUnderTest;

    @Test
    void that_only_tags_matching_semver_pattern_are_found() {
        classUnderTest = createCollector("\\d+[.]\\d+[.]\\d+");
        Map<ObjectId,String> taggedRefs = classUnderTest.collect();
        assertFalse(taggedRefs.isEmpty());
        assertTrue(taggedRefs.size() >= 10);

        Set<String> tags = taggedRefs.entrySet().stream().map(e->e.getValue()).collect(Collectors.toSet());
        assertTrue(tags.contains("refs/tags/0.0.1"));
        assertFalse(tags.contains("just-a-test/issue-9"));
    }

    @Test
    void that_matching_arbitrary_pattern_are_found() {
        classUnderTest = createCollector(".*issue.*");
        Map<ObjectId,String> taggedRefs = classUnderTest.collect();
        assertFalse(taggedRefs.isEmpty());
        assertEquals(1, taggedRefs.size());

        Set<String> tags = taggedRefs.entrySet().stream().map(e->e.getValue()).collect(Collectors.toSet());
        assertFalse(tags.contains("just-a-test/issue-9"));
    }

    private GitTagCollector createCollector(String regexPattern) {
        Path workingDir = Paths.get(".");
        GitTagVersionHelper versionHelper = new GitTagVersionHelper(null, workingDir);
        Repository repository = versionHelper.getRepository();
        return new GitTagCollector(repository, regexPattern, null);
    }

}
