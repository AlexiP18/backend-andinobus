package com.andinobus.backendsmartcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = "com.andinobus.backendsmartcode")
@EnableJpaRepositories(basePackages = "com.andinobus.backendsmartcode")
@EntityScan(basePackages = "com.andinobus.backendsmartcode")
public class BackendSmartcodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendSmartcodeApplication.class, args);
    }

}
