package net.raumzeitfalle.gradle.gocd.versioning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class GocdVersionBuilderTest {

    private GocdVersionBuilder classUnderTest;

    private GocdEnvironment environment;

    private GocdVersionPluginExtension extension;

    private String givenProjectVersion;

    @BeforeEach
    public void prepareTest() {
        extension = new GocdVersionPluginExtension();
        extension.setTimestampSupplier(() -> LocalDateTime.of(2021, 9, 4, 19, 22, 13));
        environment = new GocdEnvironmentImpl(new HashMap<>());
        givenProjectVersion = "1.0";
    }

    @Test
    void that_pipeline_counter_is_appended_to_undefined_version() {
        environment.setEnvVariable(EnvironmentVariables.COMPUTERNAME, "BUILDAGENT");
        environment.setEnvVariable(EnvironmentVariables.GO_PIPELINE_COUNTER, "156.2");
        givenProjectVersion = null;
        classUnderTest = new GocdVersionBuilder(environment, extension, givenProjectVersion ,null);

        assertEquals("undefined.156.2", classUnderTest.build());
    }

    @Test
    void that_pipeline_counter_is_appended_to_project_version() {
        environment.setEnvVariable(EnvironmentVariables.COMPUTERNAME, "BUILDAGENT");
        environment.setEnvVariable(EnvironmentVariables.GO_PIPELINE_COUNTER, "156.2");
        givenProjectVersion = "20210912.0";
        classUnderTest = new GocdVersionBuilder(environment, extension, givenProjectVersion, null);

        assertEquals("20210912.0.156.2", classUnderTest.build());
    }

    @Test
    void that_localbuild_is_added_when_project_is_not_executed_in_GOCD_environment() {
        environment.setEnvVariable(EnvironmentVariables.COMPUTERNAME, "BUILDAGENT");
        givenProjectVersion = "1.0";
        classUnderTest = new GocdVersionBuilder(environment, extension, givenProjectVersion, null);

        assertEquals("1.0.BUILDAGENT.20210904192213", classUnderTest.build());
    }

    @Test
    void that_timestamp_in_version_is_properly_created_even_with_missing_supplier_of_time() {
        environment.setEnvVariable(EnvironmentVariables.COMPUTERNAME, "LOCALHOST");
        givenProjectVersion = "17.2";
        extension.setTimestampSupplier(null);

        classUnderTest = new GocdVersionBuilder(environment, extension, givenProjectVersion, null);

        String newVersion = classUnderTest.build();
        int lastDot = newVersion.lastIndexOf('.');

        assertTrue(lastDot > 1);
        assertTrue(newVersion.startsWith("17.2.LOCALHOST"));

        String timestamp = newVersion.substring(lastDot+1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(extension.getTimestampPattern());
        assertDoesNotThrow(()->LocalDateTime.parse(timestamp,formatter));
    }

    @Test
    void that_localbuild_uses_custom_versioning_schema() {
        environment.setEnvVariable(EnvironmentVariables.COMPUTERNAME, "BUILDAGENT");
        givenProjectVersion = "1.0-LOCAL-BUILD";
        classUnderTest = new GocdVersionBuilder(environment, extension, givenProjectVersion, "1.0-AUTOMATED-BUILD");

        assertEquals("1.0-LOCAL-BUILD.BUILDAGENT.20210904192213", classUnderTest.build());
    }

    @Test
    void that_autobuild_uses_custom_versioning_schema() {
        environment.setEnvVariable(EnvironmentVariables.COMPUTERNAME, "BUILDAGENT");
        environment.setEnvVariable(EnvironmentVariables.GO_PIPELINE_COUNTER, "124.1");
        givenProjectVersion = "1.0-LOCAL-BUILD";
        classUnderTest = new GocdVersionBuilder(environment, extension, givenProjectVersion, "1.0-AUTOMATED-BUILD");

        assertEquals("1.0-AUTOMATED-BUILD.124.1", classUnderTest.build());
    }

}
