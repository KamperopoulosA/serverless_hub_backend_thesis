package com.serverless.platformselector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ServerlessPlatformSelectorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerlessPlatformSelectorApplication.class, args);
    }
}
