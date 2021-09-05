package net.raumzeitfalle.gradle.gocd.versioning;

public interface GocdEnvironment {

    void setEnvVariable(EnvironmentVariables variable, String value);
    String getComputerName();
    String getPipelineCounter();
    boolean isAutomatedBuild();

}
