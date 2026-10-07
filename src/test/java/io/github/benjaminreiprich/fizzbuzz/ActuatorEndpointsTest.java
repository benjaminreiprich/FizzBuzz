package io.github.benjaminreiprich.fizzbuzz;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Real HTTP calls: the management port only exists when the embedded server actually starts. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "management.server.port=0")
class ActuatorEndpointsTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final int serverPort;
    private final int managementPort;

    ActuatorEndpointsTest(@LocalServerPort int serverPort, @LocalManagementPort int managementPort) {
        this.serverPort = serverPort;
        this.managementPort = managementPort;
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"})
    void should_report_up_on_health_probes(String path) throws Exception {
        HttpResponse<String> response = get(managementPort, path);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void should_expose_request_metrics_in_prometheus_format() throws Exception {
        get(serverPort, "/api/v1/fizzbuzz?int1=3&int2=5&limit=15&str1=fizz&str2=buzz");

        HttpResponse<String> response = get(managementPort, "/actuator/prometheus");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("http_server_requests_seconds_count{")
                .contains("uri=\"/api/v1/fizzbuzz\"");
    }

    // Only health and prometheus are exposed: the others can reveal configuration, internals or memory content.
    @ParameterizedTest
    @ValueSource(strings = {"env", "beans", "configprops", "heapdump", "threaddump", "loggers", "mappings", "metrics"})
    void should_not_expose_other_actuator_endpoints(String endpoint) throws Exception {
        assertThat(get(managementPort, "/actuator/" + endpoint).statusCode()).isEqualTo(404);
    }

    @Test
    void should_not_expose_actuator_on_the_public_port() throws Exception {
        assertThat(get(serverPort, "/actuator/health").statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> get(int port, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .build();
        return httpClient.send(request, BodyHandlers.ofString());
    }
}
