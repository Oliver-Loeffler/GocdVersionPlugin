package net.raumzeitfalle.gradle.gocd.versioning;

import static net.raumzeitfalle.gradle.gocd.versioning.EnvMap.mapOf;
import static net.raumzeitfalle.gradle.gocd.versioning.EnvMap.mapOf3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GocdVersionClosureTest {

    private static Project project;
    
    GocdVersionPluginExtension ext = null;

    @BeforeAll
    public static void prepareProject() {
        project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("net.raumzeitfalle.gradle.gocdversion");
    }
    
    @BeforeEach
    void prepare() {
        ext = new GocdVersionPluginExtension();
        ext.setTimestampSupplier(()-> LocalDateTime.of(2012,1,2,13,22,10));
    }

    @Test
    void that_closure_is_correctly_wired_with_version_builder() {

        Supplier<GocdEnvironment> env = ()->new GocdEnvironmentImpl(project,mapOf(GOCD.COMPUTERNAME, "HAL9000"));

        GocdVersionClosure closure = new GocdVersionClosure(project,env,ext, this);

        Object returnValue = closure.doCall("AUTO-VERSION", "MANUAL-VERSION");

        assertTrue(returnValue instanceof GocdVersionBuilder);
        assertEquals("MANUAL-VERSION.HAL9000.20120102132210" +
                "", ((GocdVersionBuilder) returnValue).build());
    }

    @Test
    void that_auto_version_is_properly_generated_by_closure() {
        Supplier<GocdEnvironment> env = ()->new GocdEnvironmentImpl(project, 
                                                                    mapOf3(GOCD.COMPUTERNAME,        "HAL9000",
                                                                           GOCD.GO_PIPELINE_COUNTER, "123",
                                                                           GOCD.GO_STAGE_COUNTER,    "1"));

        GocdVersionClosure closure = new GocdVersionClosure(project,env,ext, this);

        Object returnValue = closure.doCall("AUTO-VERSION", "MANUAL-VERSION");

        assertTrue(returnValue instanceof GocdVersionBuilder);
        assertEquals("AUTO-VERSION.123.1", ((GocdVersionBuilder) returnValue).build());
    }

}
