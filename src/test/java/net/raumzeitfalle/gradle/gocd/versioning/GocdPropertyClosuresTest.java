package net.raumzeitfalle.gradle.gocd.versioning;

import static net.raumzeitfalle.gradle.gocd.versioning.EnvMap.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.function.Supplier;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import groovy.lang.Closure;

class GocdPropertyClosuresTest<X> {

    private static Project project;
    
    private Supplier<GocdEnvironment> env;
    
    private Closure<?> classUnderTest;

    @BeforeAll
    public static void prepareProject() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("net.raumzeitfalle.gradle.gocdversion");
    }

    @Test
    void that_pipeline_counter_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_COUNTER, 12));
        classUnderTest = new GocdPipelineCounterClosure(env, this);
        assertEquals(12, classUnderTest.call());
    }
    
    @Test
    void that_missing_pipeline_counter_results_in_pipeline_count_zero() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdPipelineCounterClosure(env, this);
        assertEquals(0, classUnderTest.call());
    }

    @Test
    void that_pipeline_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_NAME, "projectx-build"));
        classUnderTest = new GocdPipelineNameClosure(env, this);
        assertEquals("projectx-build", classUnderTest.call());
    }
    
    @Test
    void that_missing_pipeline_name_results_in_empty_string() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdPipelineNameClosure(env, this);
        assertEquals("", classUnderTest.call());
    }
    
    @Test
    void that_pipeline_label_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_LABEL, "245.12"));
        classUnderTest = new GocdPipelineLabelClosure(env, this);
        assertEquals("245.12", classUnderTest.call());
    }
    
    @Test
    void that_missing_pipeline_label_results_in_empty_string() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdPipelineLabelClosure(env, this);
        assertEquals("", classUnderTest.call());
    }
    
    @Test
    void that_pipeline_group_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_GROUP_NAME, "engineering"));
        classUnderTest = new GocdPipelineGroupNameClosure(env, this);
        assertEquals("engineering", classUnderTest.call());
    }
    
    @Test
    void that_missing_pipeline_group_name_results_in_empty_string() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdPipelineGroupNameClosure(env, this);
        assertEquals("", classUnderTest.call());
    }
    
    @Test
    void that_stage_counter_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_STAGE_COUNTER, 17));
        classUnderTest = new GocdStageCounterClosure(env, this);
        assertEquals(17, classUnderTest.call());
    }
    
    @Test
    void that_trigger_user_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_TRIGGER_USER, "root"));
        classUnderTest = new GocdTriggerUserClosure(env, this);
        assertEquals("root", classUnderTest.call());
    }
    
    @Test
    void that_trigger_user_refers_to_USERNAME_variable_when_TRIGGER_USER_variable_is_missing() {
        env = () -> new GocdEnvironmentImpl(project, mapOf("USERNAME", "windowsroot"));
        classUnderTest = new GocdTriggerUserClosure(env, this);
        assertEquals("windowsroot", classUnderTest.call());
    }
    
    @Test
    void that_trigger_user_refers_to_USER_variable_when_TRIGGER_USER_variable_is_missing() {
        env = () -> new GocdEnvironmentImpl(project, mapOf("USER", "otherroot"));
        classUnderTest = new GocdTriggerUserClosure(env, this);
        assertEquals("otherroot", classUnderTest.call());
    }
    
    @Test
    void that_trigger_user_refers_to_whoami_response_when_no_variables_are_defined() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdTriggerUserClosure(env, this);
        
        Object determinedName = classUnderTest.call();
        assertTrue(String.valueOf(determinedName).length() > 0);
        assertFalse("".equalsIgnoreCase(String.valueOf(determinedName).trim()));
    }
    
    @Test
    void that_build_is_detected_as_automated_when_GO_PIPELINE_COUNTER_variable_exists() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_COUNTER, "17"));
        classUnderTest = new GocdIsAutomatedBuildClosure(env, this);
        assertTrue((boolean) classUnderTest.call());
    }
    
    @Test
    void that_build_is_detected_as_local_when_GO_PIPELINE_COUNTER_variable_is_missing() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdIsAutomatedBuildClosure(env, this);
        assertFalse((boolean) classUnderTest.call());
    }
    
    @Test
    void that_a_material_branch_name_is_properly_returned_when_available() {
        String materialName = "GITBUCKET";
        String branchName = "production";
        env = () -> new GocdEnvironmentImpl(project, 
                                            mapOf(GOCD.GO_MATERIAL_BRANCH.toString()+"_"+materialName, branchName));
        classUnderTest = new GocdGitMaterialBranchNameClosure(env, this);
        assertEquals("production", classUnderTest.call(materialName));
    }

    @Test
    void that_an_error_is_raised_when_material_name_is_null() {
        String materialName = "GITBUCKET";
        String branchName = "production";
        env = () -> new GocdEnvironmentImpl(project, 
                                            mapOf(GOCD.GO_MATERIAL_BRANCH.toString()+"_"+materialName, branchName));
        classUnderTest = new GocdGitMaterialBranchNameClosure(env, this);
        assertThrows(IllegalArgumentException.class, ()->classUnderTest.call((String) null));
    }
    
    @Test
    void that_an_error_is_raised_when_material_name_is_blank() {
        env = () -> new GocdEnvironmentImpl(project,Collections.emptyMap());
        classUnderTest = new GocdGitMaterialBranchNameClosure(env, this);
        assertThrows(IllegalArgumentException.class, ()->classUnderTest.call(""));
    }
    
    @Test
    void that_material_branch_name_is_blank_string_when_not_defined_in_environment() {
        env = () -> new GocdEnvironmentImpl(project,Collections.emptyMap());
        classUnderTest = new GocdGitMaterialBranchNameClosure(env, this);
        assertEquals("", classUnderTest.call("GITBUCKET"));
    }
    
    @Test
    void that_computername_is_determined_via_hostname_command_when_not_in_environment() {
        env = () -> new GocdEnvironmentImpl(project,Collections.emptyMap());
        classUnderTest = new GocdComputerNameClosure(env, this);
        
        String expectedHost = tryHostname();
        assertEquals(expectedHost, classUnderTest.call());
    }
    
    @Test
    void that_computername_is_read_from_environment() {
        env = () -> new GocdEnvironmentImpl(project,mapOf(GOCD.COMPUTERNAME, "ENIAC_COMPUTER"));
        classUnderTest = new GocdComputerNameClosure(env, this);
        assertEquals("ENIAC_COMPUTER", classUnderTest.call());
    }
    
    @Test
    void that_stage_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_STAGE_NAME, "build_stage"));
        classUnderTest = new GocdStageNameClosure(env, this);
        assertEquals("build_stage", classUnderTest.call());
    }
    
    @Test
    void that_missing_stage_name_results_in_empty_string() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdStageNameClosure(env, this);
        assertEquals("", classUnderTest.call());
    }
    
    @Test
    void that_environment_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_ENVIRONMENT_NAME, "production_environment"));
        classUnderTest = new GocdEnvironmentNameClosure(env, this);
        assertEquals("production_environment", classUnderTest.call());
    }
    
    @Test
    void that_missing_environment_name_results_in_empty_string() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdEnvironmentNameClosure(env, this);
        assertEquals("", classUnderTest.call());
    }

    @Test
    void that_job_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_JOB_NAME, "package-job"));
        classUnderTest = new GocdJobNameClosure(env, this);
        assertEquals("package-job", classUnderTest.call());
    }
    
    @Test
    void that_missing_job_name_results_in_empty_string() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdJobNameClosure(env, this);
        assertEquals("", classUnderTest.call());
    }
    
    @Test
    void that_server_url_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_SERVER_URL, "http://gocd.company.com:2356"));
        classUnderTest = new GocdServerUrlClosure(env, this);
        assertEquals("http://gocd.company.com:2356", classUnderTest.call());
    }
    
    @Test
    void that_missing_server_url_name_results_in_empty_string() {
        env = () -> new GocdEnvironmentImpl(project, Collections.emptyMap());
        classUnderTest = new GocdServerUrlClosure(env, this);
        assertEquals("", classUnderTest.call());
    }
    
    private String tryHostname() {
        ProcessBuilder pb = new ProcessBuilder("hostname");
        Process process;
        try {
            process = pb.start();
            String line = null;
            try (InputStreamReader isr = new InputStreamReader(process.getInputStream()); BufferedReader reader = new BufferedReader(isr)) {
                while ((line = reader.readLine()) != null) {
                    return line;
                }
                return "<hostname command failed>";
            }
        } catch (IOException e) {
            return "<hostname command failed>";
        }
    }
}
