package se.rocketscien.mcp.springpgdbmcpserver.config;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties
@Value
public class ServerListConfig {

    List<ServerConfig> servers;

    /**
     * Bearer token -> servers the token may access. A null {@code readonly} is inherited
     * from the server's own config, a set value overrides it for this token.
     */
    Map<String, List<ServerAccess>> authTokens;

    public record ServerAccess(String server, Boolean readonly) {}

    public record ServerConfig(
            String name,
            String host,
            int port,
            String username,
            String password,
            String database,
            boolean readonly
    ) {

        public String getJdbcUrl() {
            return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        }
    }
}
