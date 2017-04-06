package com.example.config;

import com.example.GoogleCloudPlatformManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(GoogleCloudPlatformManager.class)
@EnableConfigurationProperties(GoogleCloudProperties.class)
public class GoogleAutoConfiguration {

    @Bean
    protected GoogleCloudPlatformManager gcpManager(GoogleCloudProperties googleCloudProperties) {
        return new GoogleCloudPlatformManager(googleCloudProperties);
    }
}
