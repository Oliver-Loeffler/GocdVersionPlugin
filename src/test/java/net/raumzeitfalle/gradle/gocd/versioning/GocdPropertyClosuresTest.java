package net.raumzeitfalle.gradle.gocd.versioning;

import static net.raumzeitfalle.gradle.gocd.versioning.EnvMap.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void that_pipeline_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_NAME, "projectx-build"));
        classUnderTest = new GocdPipelineNameClosure(env, this);
        assertEquals("projectx-build", classUnderTest.call());
    }
    
    @Test
    void that_pipeline_label_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_LABEL, "245.12"));
        classUnderTest = new GocdPipelineLabelClosure(env, this);
        assertEquals("245.12", classUnderTest.call());
    }
    
    @Test
    void that_pipeline_group_name_is_available_from_closure() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_GROUP_NAME, "engineering"));
        classUnderTest = new GocdPipelineGroupNameClosure(env, this);
        assertEquals("engineering", classUnderTest.call());
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
    void that_build_is_detected_as_automated_when_PGO_PIPELINE_COUNTER_variable_exists() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.GO_PIPELINE_COUNTER, "17"));
        classUnderTest = new GocdIsAutomatedBuildClosure(env, this);
        assertTrue((boolean) classUnderTest.call());
    }
    
    @Test
    void that_build_is_detected_as_local_when_PGO_PIPELINE_COUNTER_variable_is_missing() {
        env = () -> new GocdEnvironmentImpl(project, mapOf(GOCD.COMPUTERNAME, "ENIAC"));
        classUnderTest = new GocdIsAutomatedBuildClosure(env, this);
        assertFalse((boolean) classUnderTest.call());
    }
    
    @Test
    void that_a_material_branch_name_is_properly_returned_when_available() {
        String materialName = "GITBUCKET";
        String branchName = "production";
        env = () -> new GocdEnvironmentImpl(project, 
                                            mapOf(GOCD.GO_MATERIAL_BRANCH.toString()+"_"+materialName, branchName));
        classUnderTest = new GocdMaterialBranchNameClosure(env, this);
        assertEquals("production", classUnderTest.call(materialName));
    }

    @Test
    void that_an_error_is_raised_when_material_name_is_null() {
        String materialName = "GITBUCKET";
        String branchName = "production";
        env = () -> new GocdEnvironmentImpl(project, 
                                            mapOf(GOCD.GO_MATERIAL_BRANCH.toString()+"_"+materialName, branchName));
        classUnderTest = new GocdMaterialBranchNameClosure(env, this);
        assertThrows(IllegalArgumentException.class, ()->classUnderTest.call((String) null));
    }
    
    @Test
    void that_an_error_is_raised_when_material_name_is_blank() {
        env = () -> new GocdEnvironmentImpl(project,Collections.emptyMap());
        classUnderTest = new GocdMaterialBranchNameClosure(env, this);
        assertThrows(IllegalArgumentException.class, ()->classUnderTest.call(""));
    }
    
    @Test
    void that_material_branch_name_is_blank_string_when_not_defined_in_environment() {
        env = () -> new GocdEnvironmentImpl(project,Collections.emptyMap());
        classUnderTest = new GocdMaterialBranchNameClosure(env, this);
        assertEquals("", classUnderTest.call("GITBUCKET"));
    }
       
}
