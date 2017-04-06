package com.example;

import com.google.api.services.storage.model.StorageObject;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@SpringBootApplication
public class StorageDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageDemoApplication.class, args);
    }

    @RestController
    class StorageDemoController {

        private final GoogleCloudPlatformManager gcpManager;

        public StorageDemoController(GoogleCloudPlatformManager gcpManager) {
            this.gcpManager = gcpManager;
        }

        @GetMapping("/")
        public CloudFoundryDetails getCloudFoundryDetails(@Value("${CF_INSTANCE_INDEX:0}") int instance,
                                                          @Value("${cloud.application.version}") String version) {
            return new CloudFoundryDetails(instance, version, Iterables.size(gcpManager.getAppBucketItems()));
        }

        @RequestMapping("/upload")
        public StorageObject upload(HttpServletRequest req) {
            UriComponents url = ServletUriComponentsBuilder.fromServletMapping(req).path("/metrics").build();

            // Generate a random name for the JSON upload
            String objectName = UUID.randomUUID().toString().concat(".json");

            // Get a snapshot of the metrics endpoint from actuator
            String metricsJson = new RestTemplate().getForObject(url.toString(), String.class);
            InputStream objectData = new ByteArrayInputStream(metricsJson.getBytes());

            // Upload the metrics snapshot to Google Storage
            return gcpManager.uploadObject(objectName, objectData);
        }

    }
}
