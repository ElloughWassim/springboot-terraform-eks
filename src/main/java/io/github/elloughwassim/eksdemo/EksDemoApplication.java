/*
 * Copyright (c) 2026 ElloughWassim
 * SPDX-License-Identifier: MIT
 */
package io.github.elloughwassim.eksdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point of the demo REST service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EksDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EksDemoApplication.class, args);
    }
}
