/*
 * Copyright (c) 2026 ElloughWassim
 * SPDX-License-Identifier: MIT
 */
package io.github.elloughwassim.eksdemo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;

/**
 * Integration test: starts the real embedded server on a random port and calls it over HTTP.
 * Executed by the Maven Failsafe plugin during {@code mvn verify}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
                properties = {"app.data.name=it-name", "app.data.value=it-value", "app.data.place=it-place"})
class HomeEndpointIT {

    private final HttpClient client = HttpClient.newHttpClient();

    @Autowired
    private Environment environment;

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        String port = environment.getRequiredProperty("local.server.port");
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void homeDataReturnsConfiguredValues() throws Exception {
        HttpResponse<String> response = get("/home/data");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
            .contains("\"id\":1")
            .contains("\"name\":\"it-name\"")
            .contains("\"place\":\"it-place\"")
            .contains("\"value\":\"it-value\"");
    }

    @Test
    void kubernetesProbesAreUp() throws Exception {
        assertThat(get("/actuator/health/liveness").statusCode()).isEqualTo(200);
        assertThat(get("/actuator/health/readiness").statusCode()).isEqualTo(200);
    }
}
