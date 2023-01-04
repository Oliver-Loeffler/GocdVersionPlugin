package net.raumzeitfalle.gradle.gocd.versioning;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

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
    
    @ParameterizedTest
    @CsvSource({
        // closure name, simple class name 
        "gocdEnvironmentName,        GocdEnvironmentNameClosure",
        "gocdPipelineLabel ,         GocdPipelineLabelClosure",
        "gocdPipelineCounter ,       GocdPipelineCounterClosure",
        "gocdStageCounter ,          GocdStageCounterClosure",
        "isAutomatedBuild ,          GocdIsAutomatedBuildClosure",
        "gocdComputerName ,          GocdComputerNameClosure",
        "gocdStageName ,             GocdStageNameClosure",
        "gocdTriggerUser ,           GocdTriggerUserClosure",
        "jpackageVersion ,           JPackageVersionClosure",
        "gocdGitMaterialBranchName , GocdGitMaterialBranchNameClosure",
        "gocdPipelineGroupName ,     GocdPipelineGroupNameClosure",
        "gocdJobName ,               GocdJobNameClosure",
        "gocdVersion ,               GocdVersionClosure",
        "gocdPipelineName ,          GocdPipelineNameClosure",
        "gocdServerUrl,              GocdServerUrlClosure"
    })
    void that_closures_are_registered_correctly(String closureName, String implementationSimpleClassName) {
        project.getPluginManager().apply("net.raumzeitfalle.gradle.gocdversion");
        Map<String,Object> closures = project.getExtensions().getExtraProperties().getProperties();
        
        assertAll(
                () -> assertTrue(closures.containsKey(closureName), "Closure name must exist as key"),
                () -> assertEquals(implementationSimpleClassName, closures.get(closureName).getClass().getSimpleName())
        );
    }
    

}
