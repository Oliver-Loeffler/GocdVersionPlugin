package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.HashMap;
import java.util.Map;

public class EnvMap {
    
    public static Map<String, String> mapOf(String variable, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(variable, value);
        return map;
    }

    public static Map<String, String> mapOf(GithubActions variable, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(variable.toString(), value);
        return map;
    }

    public static Map<String, String> mapOf(GitlabCICD variable, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(variable.toString(), value);
        return map;
    }
    
    public static Map<String, String> mapOf(GOCD variable, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(variable.toString(), value);
        return map;
    }
    
    public static Map<String, String> mapOf(GOCD variable, int value) {
        Map<String, String> map = new HashMap<>();
        map.put(variable.toString(), Integer.toString(value));
        return map;
    }
    
    public static Map<String, String> mapOf2(GOCD variable1, String value1, 
                                      GOCD variable2, String value2) {
        Map<String, String> map = new HashMap<>();
        map.put(variable1.toString(), value1);
        map.put(variable2.toString(), value2);
        return map;
    }

    public static Map<String, String> mapOf3(GOCD variable1, String value1, 
                                      GOCD variable2, String value2, 
                                      GOCD variable3, String value3) {
        Map<String, String> map = new HashMap<>();
        map.put(variable1.toString(), value1);
        map.put(variable2.toString(), value2);
        map.put(variable3.toString(), value3);
        return map;
    }
}
