package com.example;

import com.example.config.GoogleCloudProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

public class GoogleCloudPlatformManager {

    private final GoogleCloudProperties googleCloudProperties;

    @Value("${spring.application.name}")
    private String appName;

    private GoogleCredential credential;

    public GoogleCloudPlatformManager(GoogleCloudProperties googleCloudProperties) {
        this.googleCloudProperties = googleCloudProperties;
    }

    public Storage getStorageClient() {
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential cred = credential();
        if (cred.createScopedRequired()) {
            cred = cred.createScoped(StorageScopes.all());
        }
        return new Storage.Builder(transport, jsonFactory, cred).setApplicationName(appName).build();
    }

    private GoogleCredential credential() {
        if (credential == null) {
            InputStream stream = new ByteArrayInputStream(Base64.getDecoder()
                    .decode(googleCloudProperties.getCredentials().getPrivateKey()));
            try {
                credential = GoogleCredential.fromStream(stream);
            } catch (IOException e) {
                throw new RuntimeException("Could not decode private key from environment", e);
            }
        }
        return credential;
    }

    public Iterable<StorageObject> getAppBucketItems() {
        List<List<StorageObject>> pagedList = Lists.newArrayList();
        Storage.Objects.List listObjects;
        Objects objects;
        String bucketName = googleCloudProperties.getCredentials().getBucketName();

        try {
            listObjects = getStorageClient().objects().list(bucketName);
        } catch (IOException ex) {
            throw new RuntimeException("Could not retrieve bucket list", ex);
        }

        do {
            try {
                objects = listObjects.execute();
            } catch (IOException ex) {
                throw new RuntimeException("Could not execute read on bucket object list", ex);
            }

            List<StorageObject> items = objects.getItems();

            if (items != null) {
                pagedList.add(objects.getItems());
            }

            listObjects.setPageToken(objects.getNextPageToken());
        } while (objects.getNextPageToken() != null);

        return Iterables.concat(pagedList);
    }

    public StorageObject uploadObject(String objectName, InputStream data) {
        StorageObject result;

        InputStreamContent mediaContent = new InputStreamContent(MediaType.APPLICATION_JSON.toString(), data);

        try {
            Storage.Objects.Insert insertObject = getStorageClient().objects()
                    .insert(googleCloudProperties.getCredentials().getBucketName(), null, mediaContent)
                    .setName(objectName);

            insertObject.getMediaHttpUploader().setDisableGZipContent(true);

            result = insertObject.execute();
        } catch (IOException e) {
            throw new RuntimeException("Could not upload storage object to GCP", e);
        }

        return result;
    }
}
