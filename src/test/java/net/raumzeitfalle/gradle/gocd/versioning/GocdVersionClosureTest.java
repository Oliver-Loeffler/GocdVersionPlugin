package net.raumzeitfalle.gradle.gocd.versioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GocdVersionClosureTest {

    GocdVersionPluginExtension ext = null;

    @BeforeEach
    void prepare() {
        ext = new GocdVersionPluginExtension();
        ext.setTimestampSupplier(()-> LocalDateTime.of(2012,1,2,13,22,10));
    }

    @Test
    void that_closure_is_correctly_wired_with_version_builder() {

        Supplier<GocdEnvironment> env = ()->new GocdEnvironmentImpl(mapOf(EnvironmentVariables.COMPUTERNAME, "HAL9000"));

        GocdVersionClosure closure = new GocdVersionClosure(env,ext, this);

        Object returnValue = closure.doCall("AUTO-VERSION", "MANUAL-VERSION");

        assertTrue(returnValue instanceof GocdVersionBuilder);
        assertEquals("MANUAL-VERSION.HAL9000.20120102132210" +
                "", ((GocdVersionBuilder) returnValue).build());
    }

    @Test
    void that_auto_version_is_properly_generated_by_closure() {

        Supplier<GocdEnvironment> env = ()->new GocdEnvironmentImpl(mapOf(EnvironmentVariables.COMPUTERNAME, "HAL9000",
                                                                          EnvironmentVariables.GO_PIPELINE_COUNTER, "123.1"));

        GocdVersionClosure closure = new GocdVersionClosure(env,ext, this);

        Object returnValue = closure.doCall("AUTO-VERSION", "MANUAL-VERSION");

        assertTrue(returnValue instanceof GocdVersionBuilder);
        assertEquals("AUTO-VERSION.123.1", ((GocdVersionBuilder) returnValue).build());

    }
    
    private Map<String,String> mapOf(EnvironmentVariables variable, String value) {
        Map<String,String> map = new HashMap<>();
        map.put(variable.toString(), value);
        return map;
    }
    
    private Map<String,String> mapOf(EnvironmentVariables variable1, String value1,
                                     EnvironmentVariables variable2, String value2) {
        Map<String,String> map = new HashMap<>();
        map.put(variable1.toString(), value1);
        map.put(variable2.toString(), value2);
        return map;
    }
}
