package net.raumzeitfalle.gradle.gocd.versioning;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gradle.api.Project;

public class GocdEnvironmentImpl implements GocdEnvironment {

    private final Map<String,String> environment;
    
    private final Project project;

    public GocdEnvironmentImpl(Project project, Map<String,String> systemEnvironment) {
        this.environment = Objects.requireNonNull(systemEnvironment, "systemEnvironment must not be null");
        this.project = Objects.requireNonNull(project);
    }

    @Override
    public void setEnvVariable(GOCD variable, String value) {
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
    public String getPipelineGroupName() {
        return getEnvOrDefault(GOCD.GO_PIPELINE_GROUP_NAME, ()->"");
    }
    
    @Override
    public String getEnvironmentName() {
        return getEnvOrDefault(GOCD.GO_ENVIRONMENT_NAME, ()->"");
    }
    
    @Override
    public int getPipelineCounter() {
        return fromEnvOrDefault(GOCD.GO_PIPELINE_COUNTER, 0);
    }
    
    @Override
    public String getPipelineLabel() {
        return getEnvOrDefault(GOCD.GO_PIPELINE_LABEL, ()->"");
    }
    
    @Override
    public String getPipelineName() {
        return getEnvOrDefault(GOCD.GO_PIPELINE_NAME, ()->"");
    }
    
    @Override
    public String getStageName() {
        return getEnvOrDefault(GOCD.GO_STAGE_NAME, ()->"");
    }
    
    @Override
    public int getStageCounter() {
        return fromEnvOrDefault(GOCD.GO_STAGE_COUNTER, 0);
    }
    
    @Override
    public String getServerUrl() {
        return getEnvOrDefault(GOCD.GO_SERVER_URL, ()->"");
    }
    
    @Override
    public String getJobName() {
        return getEnvOrDefault(GOCD.GO_JOB_NAME, ()->"");
    }

    @Override
    public String getTriggerUser() {
        return getEnvOrDefault(GOCD.GO_TRIGGER_USER, ()->"");
    }
    
    @Override
    public List<String> getAgentResources() {
        String configuredResources = getEnvOrDefault(GOCD.GO_AGENT_RESOURCES, ()->"").trim();
        if ("".equalsIgnoreCase(configuredResources)) {
            return Collections.emptyList();
        }
        return Arrays.stream(configuredResources.split(","))
                     .collect(Collectors.toList());
    }
    
    @Override
    public boolean isAutomatedBuild() {
        return environment.containsKey(GOCD.GO_PIPELINE_COUNTER.toString());
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
    
    private int fromEnvOrDefault(GOCD variable, int defaultValue) {
        String value = getEnvOrDefault(variable,
                                      ()->Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            String template = "Found unexpected value \"%s\" for GOCD env variable \"%s\"";
            project.getLogger().warn(String.format(template, value, variable.toString()));
            return defaultValue;
        }
    }
    
    private String getEnvOrDefault(GOCD variable, Supplier<String> defaultValue) {
        return getEnvOrDefault(variable.toString(), defaultValue);
    }
    
    private String getEnvOrDefault(String variable, Supplier<String> defaultValue) {
        String value = getenv(variable.toString());
        if (null == value) {
            String template = "Environment variable \"%s\" nof found!";
            project.getLogger().debug(String.format(template, variable));
            return defaultValue.get();
        } else {
            if ("".equalsIgnoreCase(value)) {
                String template = "Environment variable \"%s\" is configured with a blank String.";
                project.getLogger().debug(String.format(template, variable));
            }
            return  value;
        }
    }

    @Override
    public String get(GOCD variable) {
        return getEnvOrDefault(variable, ()->"");
    }



}
