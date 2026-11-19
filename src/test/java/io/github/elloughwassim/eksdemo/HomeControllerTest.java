/*
 * Copyright (c) 2026 ElloughWassim
 * SPDX-License-Identifier: MIT
 */
package io.github.elloughwassim.eksdemo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test: no Spring context, runs in milliseconds.
 */
class HomeControllerTest {

    @Test
    void returnsConfiguredData() {
        HomeController controller = new HomeController(new AppDataProperties("demo", "v1", "Casablanca"));

        HomeController.HomeData data = controller.getData();

        assertThat(data.id()).isEqualTo(1);
        assertThat(data.name()).isEqualTo("demo");
        assertThat(data.value()).isEqualTo("v1");
        assertThat(data.place()).isEqualTo("Casablanca");
    }
}
