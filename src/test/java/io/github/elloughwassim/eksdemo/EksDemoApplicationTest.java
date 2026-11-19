/*
 * Copyright (c) 2026 ElloughWassim
 * SPDX-License-Identifier: MIT
 */
package io.github.elloughwassim.eksdemo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the Spring context starts and binds the default configuration.
 */
@SpringBootTest(properties = {"app.data.name=test-name", "app.data.value=test-value"})
class EksDemoApplicationTest {

    @Autowired
    private AppDataProperties properties;

    @Test
    void contextLoadsAndBindsProperties() {
        assertThat(properties.name()).isEqualTo("test-name");
        assertThat(properties.value()).isEqualTo("test-value");
        assertThat(properties.place()).isEqualTo("localhost");
    }
}
