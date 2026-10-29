package se.rocketscien.mcp.springpgdbmcpserver.connections;

import org.springframework.stereotype.Service;

import se.rocketscien.mcp.springpgdbmcpserver.config.AuthException;

import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    /** Allowed servers by name, true when the token is limited to reads on that server. */
    public record AuthContext(String token, Map<String, Boolean> servers) {}

    public static final ScopedValue<AuthContext> CURRENT = ScopedValue.newInstance();

    public Set<String> allowedServers() {
        return CURRENT.isBound() ? CURRENT.get().servers().keySet() : Set.of();
    }

    public void requireServer(String name) {
        if (!CURRENT.isBound() || !CURRENT.get().servers().containsKey(name)) {
            throw new AuthException("Server '" + name + "' is not allowed for this token");
        }
    }

    public boolean isReadonly(String name) {
        return CURRENT.isBound() && Boolean.TRUE.equals(CURRENT.get().servers().get(name));
    }
}
