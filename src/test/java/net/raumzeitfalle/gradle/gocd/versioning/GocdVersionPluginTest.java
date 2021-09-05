package net.raumzeitfalle.gradle.gocd.versioning;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GocdVersionPluginTest {

    private static Project project;

    @BeforeAll
    public static void prepareProject() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("net.raumzeitfalle.gradle.gocdversion");
    }

    @Test
    void that_task_printGocdEnvironment_is_registered_correctly() {
        project.getPluginManager().apply("net.raumzeitfalle.gradle.gocdversion");
        Task task = project.getTasks().getByName("printGocdEnvironment");
        assertEquals("net.raumzeitfalle.gradle.gocd.versioning.PrintGocdEnvironmentTask_Decorated", task.getClass().getName());
    }

}
