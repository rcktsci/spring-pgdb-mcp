package se.rocketscien.mcp.springpgdbmcpserver.connections;

import org.springframework.stereotype.Service;

import se.rocketscien.mcp.springpgdbmcpserver.config.AuthException;

import java.util.Set;

@Service
public class AuthService {

    public record AuthContext(String token, Set<String> servers) {}

    public static final ScopedValue<AuthContext> CURRENT = ScopedValue.newInstance();

    public Set<String> allowedServers() {
        return CURRENT.isBound() ? CURRENT.get().servers() : Set.of();
    }

    public void requireServer(String name) {
        if (!CURRENT.isBound() || !CURRENT.get().servers().contains(name)) {
            throw new AuthException("Server '" + name + "' is not allowed for this token");
        }
    }
}
