package com.example;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.Lists;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class StorageDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageDemoApplication.class, args);
    }

    @Bean
    CredentialManager credentialManager() {
        return new CredentialManager();
    }

    @Bean
    String bucketName(CredentialManager credentialManager) {
        return credentialManager.getBucketName();
    }

    @Bean
    Storage storageClient(CredentialManager credentialManager) throws IOException {
        return credentialManager.getStorageClient();
    }

    @Bean
    Storage.Buckets.Get getStorageBucket(Storage storageClient, String bucketName) throws IOException {
        return storageClient.buckets().get(bucketName);
    }
}

@RestController
class StorageDemoController {
    @Autowired
    private Storage.Buckets.Get getStorageBucket;

    @Autowired
    private String bucketName;

    @Autowired
    private Storage storageClient;

    @RequestMapping("/")
    public CloudFoundryDemo cloudFoundryDemo(@Value("${CF_INSTANCE_INDEX:0}") int instance, @Value("${cloud.application.version}") String version) throws IOException {
        return new CloudFoundryDemo(instance, version, getObjectCount());
    }

    @RequestMapping("/upload")
    public StorageObject upload() throws IOException {
        String objectName = UUID.randomUUID().toString().concat(".json");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String objectData = gson.toJson(this);

        return ObjectUpload.uploadJson(storageClient, bucketName, objectName, objectData);
    }

    private Integer getObjectCount() throws IOException {
        Integer count = 0;
        for (StorageObject object : list()) count++;
        return count;
    }

    private Iterable<StorageObject> list() throws IOException {
        List<List<StorageObject>> pagedList = Lists.newArrayList();
        Storage.Objects.List listObjects = storageClient.objects().list(bucketName);
        Objects objects;
        do {
            objects = listObjects.execute();
            List<StorageObject> items = objects.getItems();
            if (items != null) {
                pagedList.add(objects.getItems());
            }
            listObjects.setPageToken(objects.getNextPageToken());
        } while (objects.getNextPageToken() != null);
        return Iterables.concat(pagedList);

    }
}

class CloudFoundryDemo {
    private final int instance;
    private String version;
    private Integer storageValue;

    public CloudFoundryDemo(int instance, String version, Integer storageValue) {
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

class ObjectUpload {
    public static StorageObject uploadJson(Storage storage, String bucketName, String objectName,
                                             String data) throws UnsupportedEncodingException, IOException {
        return uploadSimple(storage, bucketName, objectName, new ByteArrayInputStream(
                data.getBytes("UTF-8")), "application/json");
    }

    public static StorageObject uploadSimple(Storage storage, String bucketName, String objectName,
                                             InputStream data, String contentType) throws IOException {
        InputStreamContent mediaContent = new InputStreamContent(contentType, data);
        Storage.Objects.Insert insertObject = storage.objects().insert(bucketName, null, mediaContent)
                .setName(objectName);
        // The media uploader gzips content by default, and alters the Content-Encoding accordingly.
        // GCS dutifully stores content as-uploaded. This line disables the media uploader behavior,
        // so the service stores exactly what is in the InputStream, without transformation.
        insertObject.getMediaHttpUploader().setDisableGZipContent(true);
        return insertObject.execute();
    }
}
