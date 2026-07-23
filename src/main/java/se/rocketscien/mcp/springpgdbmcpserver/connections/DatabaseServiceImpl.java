package se.rocketscien.mcp.springpgdbmcpserver.connections;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import se.rocketscien.mcp.springpgdbmcpserver.config.ServerListConfig;
import se.rocketscien.mcp.springpgdbmcpserver.config.ServerListConfig.ServerConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
class DatabaseServiceImpl implements DatabaseService {

    private final DatabaseConnectionsManager databaseConnectionsManager;
    private final ServerListConfig serverListConfig;

    @Override
    public List<Map<String, Object>> listServers() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (var server : serverListConfig.getServers()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("name", server.name());
            info.put("host", server.host());
            info.put("port", server.port());
            info.put("database", server.database());
            info.put("readonly", server.readonly());
            result.add(info);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> query(String serverName, String sql) throws SQLException {
        var server = findServer(serverName);

        if (!isSelect(sql)) {
            throw new SQLException("Query tool only supports SELECT statements");
        }

        return executeQuery(server, sql);
    }

    @Override
    public Map<String, Object> execute(String serverName, String sql) throws SQLException {
        var server = findServer(serverName);

        if (server.readonly()) {
            throw new SQLException("Server '" + serverName + "' is configured as readonly");
        }

        if (isSelect(sql)) {
            var result = executeQuery(server, sql);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("type", "SELECT");
            response.put("rows", result);
            response.put("rowCount", result.size());
            return response;
        }

        return executeUpdate(server, sql);
    }

    private List<Map<String, Object>> executeQuery(ServerConfig server, String sql) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        try (var connection = databaseConnectionsManager.getDataSource(server).getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {

            var metaData = resultSet.getMetaData();
            var columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (var i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                results.add(row);
            }
        }

        return results;
    }

    private Map<String, Object> executeUpdate(ServerConfig server, String sql) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();

        try (var conn = databaseConnectionsManager.getDataSource(server).getConnection();
             var stmt = conn.createStatement()) {

            var affectedRows = stmt.executeUpdate(sql);

            result.put("type", sql.trim().split("\\s+")[0]);
            result.put("affectedRows", affectedRows);
        }

        return result;
    }

    private ServerConfig findServer(String name) {
        return serverListConfig.getServers()
                .stream()
                .filter(s -> s.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Server not found: " + name));
    }

    private boolean isSelect(String sql) {
        var trimmed = sql.trim().toUpperCase();
        return trimmed.startsWith("SELECT") || trimmed.startsWith("WITH");
    }
}
