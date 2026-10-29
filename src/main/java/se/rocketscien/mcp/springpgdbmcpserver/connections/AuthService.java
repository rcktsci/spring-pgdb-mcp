package se.rocketscien.mcp.springpgdbmcpserver.connections;

import org.springframework.stereotype.Service;

import se.rocketscien.mcp.springpgdbmcpserver.config.AuthException;

import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    /** Effective readonly per allowed server: the token entry's override, or the server's own config. */
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

    /** Resolved readonly for the server, or null when unauthenticated or the server is not in the token's list. */
    public Boolean tokenReadonly(String name) {
        return CURRENT.isBound() ? CURRENT.get().servers().get(name) : null;
    }
}
