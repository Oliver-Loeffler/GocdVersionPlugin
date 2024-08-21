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

public class GitTagVersionClosureTestWithCustomTag {
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

        git.tag().setName("0.13")
                .setMessage("Just another test")
                .call();

        Path otherFile = srcDir.resolve("README.md");
        Files.write(otherFile, "# README here".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

        git.add().addFilepattern(otherFile.toAbsolutePath().toString()).call();
        git.commit().setSign(false)
                .setAuthor("Junit", "test@domain.xyz")
                .setCommitter("Junit", "test@domain.xyz")
                .setMessage("one more file")
                .call();

        git.tag().setName("Ver22")
                .setMessage("Lets see again!")
                .call();
    }

    @BeforeEach
    void prepare() {
        ext = new GocdVersionPluginExtension();
        ext.setTimestampSupplier(()-> LocalDateTime.of(2012,1,2,13,22,10));
        ext.setSuitableTagRegex("^Ver\\d{2}$");
    }

    @Test
    void that_commit_count_and_git_commit_hash_are_appended_to_current_git_tag() {
        GitTagVersionClosure closure = new GitTagVersionClosure(project, ext, this);
        Object returnValue = closure.doCall();
        String determinedVersion = String.valueOf(returnValue);

        String[] versionParts = String.valueOf(returnValue).split("-");
        assertEquals(1, versionParts.length, "1 elements expected in short version number");
        assertEquals("Ver22", versionParts[0], "Tag followed by commit count since tag.");
    }

}
