/*
 * Copyright (c) 2026 ElloughWassim
 * SPDX-License-Identifier: MIT
 */
package io.github.elloughwassim.eksdemo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Values exposed by {@code GET /home/data}, bound from {@code app.data.*}.
 *
 * <p>In Kubernetes they come from a ConfigMap as environment variables
 * ({@code APP_DATA_NAME}, {@code APP_DATA_VALUE}, {@code APP_DATA_PLACE}),
 * thanks to Spring Boot relaxed binding.</p>
 *
 * @param name  display name returned by the API
 * @param value free-form configuration value returned by the API
 * @param place location label returned by the API
 */
@ConfigurationProperties(prefix = "app.data")
public record AppDataProperties(String name, String value, String place) {
}
