package net.raumzeitfalle.gradle.gocd.versioning;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.platform.commons.JUnitException;

public class TestHelper {
    public static void createDirectoryTree(Path workingDir) {
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectories(workingDir);
            } catch (IOException e) {
                throw new JUnitException("Failed to setup working directory for test.", e);
            }
        }
    }

    public static void createFakeGitRepository(Path fakeGitRoot) {
        Path gitDir = fakeGitRoot.resolve(".git");
        if (!Files.notExists(gitDir)) {
            createDirectoryTree(gitDir);
        }
        
        String message = "this_is_no_a_real_git_repo";
        Path gitFile = gitDir.resolve(message+".txt");
        if (!Files.notExists(gitFile)) {
            writeText(gitFile, message);
        }
        
    }

    private static void writeText(Path gitFile, String message) {
        try {
            Files.write(gitFile, message.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new JUnitException("Failed to prepare file for test.", e);
        }
    }
}
