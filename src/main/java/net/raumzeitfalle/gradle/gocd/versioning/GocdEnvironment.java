package net.raumzeitfalle.gradle.gocd.versioning;

public interface GocdEnvironment {

    void setEnvVariable(EnvironmentVariables variable, String value);
    String getComputerName();
    String getPipelineCounter();
    String getPipelineLabel();
    String getPipelineName();
    String getStageName();
    int getStageCounter();
    String getServerUrl();
    String getJobName();
    String getTriggerUser();
    boolean isAutomatedBuild();
    String get(EnvironmentVariables variable);
    
}
