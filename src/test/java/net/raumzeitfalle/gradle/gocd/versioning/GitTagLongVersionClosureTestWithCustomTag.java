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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitTagLongVersionClosureTestWithCustomTag {
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

        git.tag().setName("0.12")
                .setMessage("Just a test")
                .call();

        Path otherFile = srcDir.resolve("README.md");
        Files.write(otherFile, "# README here".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

        git.add().addFilepattern(otherFile.toAbsolutePath().toString()).call();
        git.commit().setSign(false)
                .setAuthor("Junit", "test@domain.xyz")
                .setCommitter("Junit", "test@domain.xyz")
                .setMessage("one more file")
                .call();

        git.tag().setName("v20")
                .setMessage("Lets see!")
                .call();
    }

    @BeforeEach
    void prepare() {
        ext = new GocdVersionPluginExtension();
        ext.setTimestampSupplier(()-> LocalDateTime.of(2012,1,2,13,22,10));
        ext.setSuitableTagRegex("^v\\d{2}$");
    }

    @Test
    void that_commit_count_and_git_commit_hash_are_appended_to_current_git_tag() {
        GitTagLongVersionClosure closure = new GitTagLongVersionClosure(project, ext, this);
        Object returnValue = closure.doCall();
        String determinedVersion = String.valueOf(returnValue);


        // Following structure is expected:
        // 0.0.0.0-f123456
        // 0.0.0 = actual tag from Git
        // 0.0.0.<commit_count_since_tag>
        // 0.0.0.0-<commit_hash> (where commit hash = short Git commit name)

        String[] versionParts = String.valueOf(returnValue).split("-");
        assertEquals(2, versionParts.length, "2 elements expected in long version number");
        assertEquals("v20.0", versionParts[0], "Tag followed by commit count since tag.");

        int shortGitNameWidth = 7;
        int separatorWidth = 1;
        String rightPart = determinedVersion.substring(determinedVersion.length()-separatorWidth-shortGitNameWidth,determinedVersion.length());
        assertTrue(rightPart.startsWith("-"));

        String shortCommitID = determinedVersion.substring(determinedVersion.length()-shortGitNameWidth,determinedVersion.length());
        assertTrue(Long.parseLong(shortCommitID, 16) >= 0, "Short Commit ID should be parseable as hex");
    }

}
