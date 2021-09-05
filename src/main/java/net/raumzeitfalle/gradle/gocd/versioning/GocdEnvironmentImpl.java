package net.raumzeitfalle.gradle.gocd.versioning;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;

public class GocdEnvironmentImpl implements GocdEnvironment {

    private final Map<String,String> environment;

    public GocdEnvironmentImpl(Map<String,String> systemEnvironment) {
        this.environment = Objects.requireNonNull(systemEnvironment, "systemEnvironment must not be null");
    }

    @Override
    public void setEnvVariable(EnvironmentVariables variable, String value) {
        Objects.requireNonNull(variable, "variable must not be null");
        if (value == null || value.isBlank()) {
            environment.remove(variable);
        } else {
            environment.put(variable.toString(), value.strip());
        }
    }

    @Override
    public String getComputerName() {
        String computerName = getenv("COMPUTERNAME");
        if (null == computerName) {
            return fromHostName();
        } else {
            return computerName;
        }
    }

    @Override
    public String getPipelineCounter() {
        String counter = getenv("GO_PIPELINE_COUNTER");
        if (null == counter) {
            return "";
        } else {
            return counter;
        }
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
}
