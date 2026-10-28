package se.rocketscien.mcp.springpgdbmcpserver.connections;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Locked;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import se.rocketscien.mcp.springpgdbmcpserver.config.ServerListConfig.ServerConfig;

import javax.sql.DataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
class DatabaseConnectionsManager {

    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    @Locked
    public DataSource getDataSource(ServerConfig config) {
        return dataSources.computeIfAbsent(
                config.name(),
                name -> createDataSource(config)
        );
    }

    @NonNull
    private static HikariDataSource createDataSource(ServerConfig config) {
        var hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());

        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setConnectionTimeout(5_000L);
        hikariConfig.setIdleTimeout(60_000L);
        hikariConfig.setMaxLifetime(600_000L);

        // pgjdbc defers setReadOnly inside a transaction, so the session default is pinned too
        if (config.readonly()) {
            hikariConfig.setReadOnly(true);
            hikariConfig.setConnectionInitSql("SET default_transaction_read_only = on");
        }

        return new HikariDataSource(hikariConfig);
    }
}
