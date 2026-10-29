package se.rocketscien.mcp.springpgdbmcpserver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import lombok.SneakyThrows;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseApplicationTest {

    protected static final String ALL_TOKEN = "550e8400-e29b-41d4-a716-446655440000";
    protected static final String RO_ONLY_TOKEN = "550e8400-e29b-41d4-a716-446655440001";
    protected static final String RO_ON_RW_TOKEN = "550e8400-e29b-41d4-a716-446655440002";
    protected static final String RW_ON_RO_TOKEN = "550e8400-e29b-41d4-a716-446655440003";

    protected static final String RW_SERVER = "rw";
    protected static final String RO_SERVER = "ro";

    protected static final PostgreSQLContainer DATABASE = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("mcp")
            .withUsername("mcp")
            .withPassword("mcp");

    /**
     * Runs at class-load time, before the Spring context reads {@code config.yml} via
     * {@code spring.config.import}: the container port is only known at runtime, so the config file
     * must be planted before the context starts. The file is deleted by a shutdown hook; the container
     * is left for Ryuk to reap when the JVM exits.
     */
    static {
        if (Files.exists(Path.of("config.yml"))) {
            throw new IllegalStateException(
                    "config.yml already exists in the test working directory; tests plant their own config.yml and refuse to overwrite it");
        }

        DATABASE.start();

        var config = """
                auth-tokens:
                  "%s":
                    - server: "%s"
                    - server: "%s"
                  "%s":
                    - server: "%s"
                  "%s":
                    - server: "%s"
                      readonly: true
                  "%s":
                    - server: "%s"
                      readonly: false
                servers:
                  - name: "%s"
                    host: "%s"
                    port: %d
                    username: "%s"
                    password: "%s"
                    database: "%s"
                    readonly: false
                  - name: "%s"
                    host: "%s"
                    port: %d
                    username: "%s"
                    password: "%s"
                    database: "%s"
                    readonly: true
                """.formatted(
                ALL_TOKEN, RW_SERVER, RO_SERVER,
                RO_ONLY_TOKEN, RO_SERVER,
                RO_ON_RW_TOKEN, RW_SERVER,
                RW_ON_RO_TOKEN, RO_SERVER,
                RW_SERVER, DATABASE.getHost(), DATABASE.getMappedPort(5432),
                DATABASE.getUsername(), DATABASE.getPassword(), DATABASE.getDatabaseName(),
                RO_SERVER, DATABASE.getHost(), DATABASE.getMappedPort(5432),
                DATABASE.getUsername(), DATABASE.getPassword(), DATABASE.getDatabaseName());

        try {
            Files.writeString(Path.of("config.yml"), config);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.deleteIfExists(Path.of("config.yml"));
            } catch (Exception ignored) {
            }
        }));
    }

    @LocalServerPort
    protected int localServerPort;

    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    protected String mcpUrl() {
        return "http://localhost:" + localServerPort + "/mcp";
    }

    @SneakyThrows
    protected HttpResponse<String> post(String token, String body, String sessionId) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(mcpUrl()))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream, application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (sessionId != null) {
            builder.header("mcp-session-id", sessionId);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
