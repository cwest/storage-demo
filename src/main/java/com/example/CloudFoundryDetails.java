package com.example;

/**
 * Provides information about the Cloud Foundry environment
 */
public class CloudFoundryDetails {

    private final int instance;
    private final String version;
    private final Integer objectCount;

    public CloudFoundryDetails(int instance, String version, Integer objectCount) {
        this.instance = instance;
        this.version = version;
        this.objectCount = objectCount;
    }

    public int getInstance() {
        return instance;
    }

    public String getVersion() {
        return version.split("-")[0];
    }

    public Integer getObjectCount() {
        return objectCount;
    }
}
