package net.raumzeitfalle.gradle.gocd.versioning;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class GocdEnvironmentImpl implements GocdEnvironment {

    private final Map<String,String> environment;

    public GocdEnvironmentImpl(Map<String,String> systemEnvironment) {
        this.environment = Objects.requireNonNull(systemEnvironment, "systemEnvironment must not be null");
    }

    @Override
    public void setEnvVariable(EnvironmentVariables variable, String value) {
        Objects.requireNonNull(variable, "variable must not be null");
        if (value == null || "".equalsIgnoreCase(value.trim())) {
            environment.remove(variable.toString());
        } else {
            environment.put(variable.toString(), value.trim());
        }
    }

    @Override
    public String getComputerName() {
        return getEnvOrDefault("COMPUTERNAME", ()->fromHostName());
    }

    @Override
    public String getPipelineCounter() {
        return getEnvOrDefault("GO_PIPELINE_COUNTER", ()->"");
    }
    
    @Override
    public String getPipelineLabel() {
        return getEnvOrDefault("GO_PIPELINE_LABEL", ()->"");
    }
    
    @Override
    public String getPipelineName() {
        return getEnvOrDefault("GO_PIPELINE_NAME", ()->"");
    }
    
    @Override
    public String getStageName() {
        return getEnvOrDefault("GO_STAGE_NAME", ()->"");
    }
    
    @Override
    public int getStageCounter() {
        return fromEnvOrDefault("GO_STAGE_COUNTER", 0);
    }
    
    @Override
    public String getServerUrl() {
        return getEnvOrDefault("GO_SERVER_URL", ()->"");
    }
    
    @Override
    public String getJobName() {
        return getEnvOrDefault("GO_JOB_NAME", ()->"");
    }
    

    @Override
    public String getTriggerUser() {
        return getEnvOrDefault("GO_TRIGGER_USER", ()->"");
    }
    
    @Override
    public boolean isAutomatedBuild() {
        return !"".equalsIgnoreCase(getPipelineCounter());
    }

    private String getenv(String name) {
        return environment.get(name);
    }

    private String fromHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException error) {
            return "LOCALBUILD";
        }
    }
    
    private int fromEnvOrDefault(String variableName, int defaultValue) {
        String value = getEnvOrDefault(variableName, ()->Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }
    
    private String getEnvOrDefault(String variableName, Supplier<String> defaultValue) {
        String value = getenv(variableName);
        if (null == value) {
            return defaultValue.get();
        } else {
            return  value;
        }
    }

    @Override
    public String get(EnvironmentVariables variable) {
        return getEnvOrDefault(variable.toString(), ()->"");
    }
}
