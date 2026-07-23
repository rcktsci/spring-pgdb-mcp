package se.rocketscien.mcp.springpgdbmcpserver.config;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties
@Value
public class ServerListConfig {

    List<ServerConfig> servers;

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
