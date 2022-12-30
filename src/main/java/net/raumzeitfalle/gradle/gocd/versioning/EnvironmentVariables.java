package net.raumzeitfalle.gradle.gocd.versioning;

public enum EnvironmentVariables {
    GO_SERVER_URL,
    
    GO_TRIGGER_USER,
    
    GO_PIPELINE_NAME,
    GO_PIPELINE_COUNTER,
    GO_PIPELINE_LABEL,
    
    GO_STAGE_NAME,
    GO_STAGE_COUNTER,
    
    GO_JOB_NAME,
    
    // TODO: this exists on windows. How about mac or linux?
    COMPUTERNAME;
}
