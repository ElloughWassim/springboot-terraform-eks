/*
 * Copyright (c) 2026 ElloughWassim
 * SPDX-License-Identifier: MIT
 */
package io.github.elloughwassim.eksdemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that returns the externally configured application data.
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    private final AppDataProperties properties;

    public HomeController(AppDataProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the configured data.
     *
     * @return JSON object with {@code id}, {@code name}, {@code place} and {@code value}
     */
    @GetMapping("/data")
    public HomeData getData() {
        return new HomeData(1, properties.name(), properties.place(), properties.value());
    }

    /**
     * Response body of {@code GET /home/data}.
     *
     * @param id    constant identifier
     * @param name  configured name
     * @param place configured place
     * @param value configured value
     */
    public record HomeData(Integer id, String name, String place, String value) {
    }
}
