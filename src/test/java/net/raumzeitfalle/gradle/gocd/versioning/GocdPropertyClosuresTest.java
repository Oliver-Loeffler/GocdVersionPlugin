package net.raumzeitfalle.gradle.gocd.versioning;

import static net.raumzeitfalle.gradle.gocd.versioning.EnvMap.mapOf;
import static org.junit.jupiter.api.Assertions.*;

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

}
