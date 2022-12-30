package net.raumzeitfalle.gradle.gocd.versioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class GocdEnvironmentTest {

    private GocdEnvironment classUnderTest;

    private Map<String,String> myEnvironment;

    @Test
    void that_pipeline_counter_is_returned_correctly_when_available() {
        myEnvironment = mapOf(EnvironmentVariables.GO_PIPELINE_COUNTER,"216.1");
        classUnderTest = new GocdEnvironmentImpl(myEnvironment);

        assertEquals("216.1", classUnderTest.getPipelineCounter());
    }

    @Test
    void that_a_build_is_automated_when_a_pipeline_counter_exists() {
        myEnvironment = mapOf(EnvironmentVariables.GO_PIPELINE_COUNTER,"216.1");
        classUnderTest = new GocdEnvironmentImpl(myEnvironment);

        assertTrue(classUnderTest.isAutomatedBuild());
    }

    @Test
    void that_the_machine_name_is_returned_properly_when_defined_in_environment() {
        myEnvironment = mapOf(EnvironmentVariables.COMPUTERNAME,"MyMachine");
        classUnderTest = new GocdEnvironmentImpl(myEnvironment);

        assertEquals("MyMachine", classUnderTest.getComputerName());
    }

    @Test
    void that_hostname_is_used_when_computername_is_not_defined_in_environment() throws Exception{
        myEnvironment = Collections.emptyMap();
        classUnderTest = new GocdEnvironmentImpl(myEnvironment);

        String expected = InetAddress.getLocalHost().getHostName();
        assertEquals(expected, classUnderTest.getComputerName());
    }
    
    private Map<String,String> mapOf(EnvironmentVariables variable, String value) {
        Map<String,String> map = new HashMap<>();
        map.put(variable.toString(), value);
        return map;
    }

}
