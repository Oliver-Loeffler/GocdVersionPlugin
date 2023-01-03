package net.raumzeitfalle.gradle.gocd.versioning;

import static net.raumzeitfalle.gradle.gocd.versioning.EnvMap.mapOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GocdEnvironmentTest {
    
    private static Project project;
    
    private GocdEnvironment classUnderTest;

    private Map<String,String> myEnvironment;

    @BeforeAll
    public static void prepareProject() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("net.raumzeitfalle.gradle.gocdversion");
    }
    
    @Test
    void that_pipeline_counter_is_returned_correctly_when_available() {
        myEnvironment = mapOf(GOCD.GO_PIPELINE_COUNTER,"216");
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);

        assertEquals(216, classUnderTest.getPipelineCounter());
    }

    @Test
    void that_a_build_is_automated_when_a_pipeline_counter_exists() {
        myEnvironment = mapOf(GOCD.GO_PIPELINE_COUNTER,"216");
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);

        assertTrue(classUnderTest.isAutomatedBuild());
    }
    
    @Test
    void that_stage_counter_is_returned_correctly_when_available() {
        myEnvironment = mapOf(GOCD.GO_STAGE_COUNTER,"12");
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);

        assertEquals(12, classUnderTest.getStageCounter());
    }
    
    @Test
    void that_no_error_occurs_when_int_variable_is_not_parseable() {
        myEnvironment = mapOf(GOCD.GO_STAGE_COUNTER,"NotANumber");
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);

        int result = assertDoesNotThrow(()->classUnderTest.getStageCounter());
        assertEquals(0, result);
    }
    
    @Test
    void that_agent_resources_are_returned_correctly_when_defined() {
        myEnvironment = mapOf(GOCD.GO_AGENT_RESOURCES,"linux,java-17,javafx-17");
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);
        assertEquals(3, classUnderTest.getAgentResources().size());
        assertEquals("linux"    , classUnderTest.getAgentResources().get(0));
        assertEquals("java-17"  , classUnderTest.getAgentResources().get(1));
        assertEquals("javafx-17", classUnderTest.getAgentResources().get(2));
    }
    
    @Test
    void that_no_errors_are_raised_when_no_agent_resources_defined() {
        myEnvironment = Collections.emptyMap();
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);
        assertTrue(classUnderTest.getAgentResources().isEmpty());
    }

    @Test
    void that_the_machine_name_is_returned_properly_when_defined_in_environment() {
        myEnvironment = mapOf(GOCD.COMPUTERNAME,"MyMachine");
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);

        assertEquals("MyMachine", classUnderTest.getComputerName());
    }

    @Test
    void that_hostname_is_used_when_computername_is_not_defined_in_environment() throws Exception{
        myEnvironment = Collections.emptyMap();
        classUnderTest = new GocdEnvironmentImpl(project,myEnvironment);

        String expected = InetAddress.getLocalHost().getHostName();
        assertEquals(expected, classUnderTest.getComputerName());
    }
}
