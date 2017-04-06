package com.example;

/**
 * Provides information about the Cloud Foundry environment
 */
public class CloudFoundryDetails {

    private final int instance;
    private final String version;
    private final Integer storageValue;

    public CloudFoundryDetails(int instance, String version, Integer storageValue) {
        this.instance = instance;
        this.version = version;
        this.storageValue = storageValue;
    }

    public int getInstance() {
        return instance;
    }

    public String getVersion() {
        return version.split("-")[0];
    }

    public Integer getStorageValue() {
        return storageValue;
    }
}
