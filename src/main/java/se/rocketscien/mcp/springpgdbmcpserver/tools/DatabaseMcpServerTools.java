package se.rocketscien.mcp.springpgdbmcpserver.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import se.rocketscien.mcp.springpgdbmcpserver.connections.DatabaseService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseMcpServerTools {

    private final DatabaseService databaseService;

    @Tool(
            name = "list",
            description = "List all configured database servers"
    )
    public List<Map<String, Object>> listServers() {
        return databaseService.listServers();
    }

    @Tool(
            name = "query",
            description = "Execute DQL (SELECT) query on a database server"
    )
    public List<Map<String, Object>> query(
            @ToolParam(description = "Server name from config") String server,
            @ToolParam(description = "SQL SELECT query") String sql
    ) {
        try {
            return databaseService.query(server, sql);
        } catch (SQLException e) {
            return List.of(Map.of("error", e.getMessage()));
        }
    }

    @Tool(
            name = "execute",
            description = "Execute DML/DDL on a database server"
    )
    public Map<String, Object> execute(
            @ToolParam(description = "Server name from config") String server,
            @ToolParam(description = "SQL INSERT/UPDATE/DELETE query") String sql
    ) {
        try {
            return databaseService.execute(server, sql);
        } catch (SQLException e) {
            return Map.of("error", e.getMessage());
        }
    }
}
