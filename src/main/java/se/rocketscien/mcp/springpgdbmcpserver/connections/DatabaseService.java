package se.rocketscien.mcp.springpgdbmcpserver.connections;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseService {

    List<Map<String, Object>> listServers();

    List<Map<String, Object>> query(String serverName, String sql) throws SQLException;

    Map<String, Object> execute(String serverName, String sql) throws SQLException;
}
