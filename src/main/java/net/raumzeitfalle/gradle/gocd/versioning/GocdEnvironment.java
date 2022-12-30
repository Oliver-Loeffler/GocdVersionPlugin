package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.List;

public interface GocdEnvironment {
    String getServerUrl();
    String getPipelineGroupName();
    String getEnvironmentName();
    
    List<String> getAgentResources();
    void setEnvVariable(GOCD variable, String value);
    
    String getPipelineLabel();
    String getPipelineName();
    int getPipelineCounter();
    
    String getStageName();
    int getStageCounter();
    
    String getJobName();
    String getTriggerUser();
    
    String get(GOCD variable);
    boolean isAutomatedBuild();
    String getComputerName();    
}
