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
     * Bearer token -> names of servers the token may access.
     */
    Map<String, List<String>> authTokens;

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
