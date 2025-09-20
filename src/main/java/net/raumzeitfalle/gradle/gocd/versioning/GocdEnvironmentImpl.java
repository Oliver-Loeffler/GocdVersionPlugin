package net.raumzeitfalle.gradle.gocd.versioning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        return getEnvOrDefault("COMPUTERNAME", ()->tryHostname(()->fromHostName()));
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
        Supplier<String> defaultUser = ()->"";
        String triggerUser = getEnvOrDefault(GOCD.GO_TRIGGER_USER, defaultUser);
        if ("".equalsIgnoreCase(triggerUser)) {
            if (environment.containsKey("USER")) {
                triggerUser = environment.get("USER");
            } else if (environment.containsKey("USERNAME")) {
                triggerUser = environment.get("USERNAME");
            } else {
                triggerUser = tryWhoami(defaultUser);
            }
        }
        return triggerUser;
    }
    
    @Override
    public String getMaterialBranch(String materialName) {
        if (null == materialName) {
            throw new IllegalArgumentException("Please provide a correct GOCD material name. A material name must not be null!");
            
        }
        
        if ("".equalsIgnoreCase(materialName)) {
            throw new IllegalArgumentException("Please provide a correct GOCD material name. A material name must not be blank!");
        }
        
        return getEnvOrDefault(GOCD.GO_MATERIAL_BRANCH.toString()
                              +"_"+String.valueOf(materialName).toUpperCase(), ()->"");
    }
    
    private String tryWhoami(Supplier<String> defaultUser) {
        try {
            return whoami(defaultUser);
        } catch (IOException e) {
            project.getLogger().warn("Failed to obtain user with execution of whoami command. Continuing with fallback.", e);
        }
        return defaultUser.get();
    }
    
    private String whoami(Supplier<String> defaultUser) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("whoami");
        Process process = pb.start();
        String line = null;
        try (InputStreamReader isr = new InputStreamReader(process.getInputStream());
             BufferedReader reader = new BufferedReader(isr)){
            while ((line=reader.readLine()) != null) {
                return line;
            }
            return defaultUser.get();
        }
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

    /**
     * Detection of automated builds is supported for GoCD, Github Actions and Gitlab CICD.
     * For GoCD, it is sufficient to have the GO_PIPELINE_COUNTER env variable declared, regardless of its value.
     * For Gitlab CI/CD the variable CI must exist and have the value true.
     * For Github Actions, a variable named GITHUB_ACTIONS=true is supposed to be available.

     * @return
     */
    @Override
    public boolean isAutomatedBuild() {
        String value = environment.get(GOCD.GO_PIPELINE_COUNTER.toString());
        if (null != value) {
            return true;
        }

        value = environment.get(GitlabCICD.CI.toString());
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        value = environment.get(GithubActions.GITHUB_ACTIONS.toString());
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        return false;
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
    
    private String tryHostname(Supplier<String> defaultHostname) {
        try {
            return hostname(defaultHostname);
        } catch (IOException e) {
            project.getLogger().warn("Failed to obtain hostname with execution of hostname command. Continuing with fallback.", e);
        }
        return defaultHostname.get();
    }
    
    private String hostname(Supplier<String> defaultHostname) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("hostname");
        Process process = pb.start();
        String line = null;
        try (InputStreamReader isr = new InputStreamReader(process.getInputStream());
             BufferedReader reader = new BufferedReader(isr)){
            while ((line=reader.readLine()) != null) {
                return line;
            }
            return defaultHostname.get();
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
            String template = "Environment variable \"%s\" not found! Going with default value \"%s\"";
            String fallback = defaultValue.get();
            project.getLogger().debug(String.format(template, variable, fallback));
            return fallback;
        } else {
            return  value;
        }
    }

    @Override
    public String get(GOCD variable) {
        return getEnvOrDefault(variable, ()->"");
    }
}
