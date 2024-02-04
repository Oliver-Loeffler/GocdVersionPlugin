package net.raumzeitfalle.gradle.gocd.versioning;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GitTagLongVersionClosureTestWithoutTag {
    private static Project project;

    private GocdVersionPluginExtension ext = null;

    @BeforeAll
    public static void prepareProject() throws IOException, GitAPIException {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("net.raumzeitfalle.gradle.gocdversion");

        Path workDir = Paths.get(project.getProjectDir().getAbsolutePath());
        Git git = Git.init().setDirectory(workDir.toFile()).setBare(false).call();

        Path srcDir = Paths.get(project.getProjectDir().getAbsolutePath(), "src");
        Files.createDirectories(srcDir);
        Path exampleFile = srcDir.resolve("EmptyProgram.java");

        Files.write(exampleFile, "\\\\ TODO: HelloWorld".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

        git.add().addFilepattern("src").call();
        git.commit().setSign(false)
                    .setAuthor("Junit", "test@domain.xyz")
                    .setCommitter("Junit", "test@domain.xyz")
                    .setMessage("start")
                    .call();

        Files.write(exampleFile, "\\\\ TODO: add some code".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        git.add().addFilepattern("src").call();
        git.commit().setSign(false)
                .setAuthor("Junit", "test@domain.xyz")
                .setCommitter("Junit", "test@domain.xyz")
                .setMessage("some change")
                .call();
    }

    @BeforeEach
    void prepare() {
        ext = new GocdVersionPluginExtension();
        ext.setTimestampSupplier(()-> LocalDateTime.now());
    }

    @Test
    void repositories_without_tags_are_considered_to_version_v0dot0() {
        ext.setMissingTagVersionDefault("v0.0");
        GitTagLongVersionClosure closure = new GitTagLongVersionClosure(project, ext, this);
        Object returnValue = closure.doCall();
        String firstPart = String.valueOf(returnValue).substring(0,4);
        assertEquals("v0.0", firstPart);
    }

    @Test
    void that_commit_count_and_git_commit_hash_are_appended_to_current_git_tag() {
        GitTagLongVersionClosure closure = new GitTagLongVersionClosure(project, ext, this);
        Object returnValue = closure.doCall();

        String determinedVersion = String.valueOf(returnValue);

        String firstPart = determinedVersion.substring(0,5);
        assertEquals("0.0.1", firstPart);

        int shortGitNameWidth = 7;
        int separatorWidth = 1;
        String rightPart = determinedVersion.substring(determinedVersion.length()-separatorWidth-shortGitNameWidth,determinedVersion.length());
        assertTrue(rightPart.startsWith("-"));

        String shortCommitID = determinedVersion.substring(determinedVersion.length()-shortGitNameWidth,determinedVersion.length());
        assertTrue(Long.parseLong(shortCommitID, 16) >= 0, "Short Commit ID should be parseable as hex");
    }

}
