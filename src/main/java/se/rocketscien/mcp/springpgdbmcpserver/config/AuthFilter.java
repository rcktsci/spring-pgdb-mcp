package se.rocketscien.mcp.springpgdbmcpserver.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ServletRequestPathUtils;

import se.rocketscien.mcp.springpgdbmcpserver.connections.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private static final String MCP_PATH = "/mcp";

    private final ServerListConfig serverListConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var context = authenticate(request);

        if (context == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"unauthorized\"}");
            return;
        }

        try {
            ScopedValue.where(AuthService.CURRENT, context)
                    .call(() -> {
                        filterChain.doFilter(request, response);
                        return null;
                    });
        } catch (ServletException | IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private AuthService.AuthContext authenticate(HttpServletRequest request) {
        var authTokens = serverListConfig.getAuthTokens();
        var header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authTokens == null || header == null || header.isBlank()) {
            return null;
        }

        var token = header.startsWith("Bearer ") ? header.substring(7).trim() : header.trim();
        if (token.isEmpty()) {
            return null;
        }

        var entries = authTokens.get(token);
        if (entries == null) {
            return null;
        }

        Map<String, Boolean> servers = new HashMap<>();
        for (var entry : entries) {
            servers.put(entry.server(), effectiveReadonly(entry));
        }
        return new AuthService.AuthContext(token, Map.copyOf(servers));
    }

    /** The entry's readonly overrides the server's own config; when absent it is inherited from it. */
    private boolean effectiveReadonly(ServerListConfig.ServerAccess entry) {
        if (entry.readonly() != null) {
            return entry.readonly();
        }
        return serverListConfig.getServers().stream()
                .filter(s -> s.name().equals(entry.server()))
                .findFirst()
                .map(ServerListConfig.ServerConfig::readonly)
                .orElse(false);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = routedPath(request);
        return !path.equals(MCP_PATH) && !path.startsWith(MCP_PATH + "/");
    }

    /**
     * The path as the {@code DispatcherServlet} routes it: percent-decoded, matrix parameters stripped and
     * without the context path. {@code getRequestURI()} must not be used here, it is the raw undecoded request
     * target, so {@code /m%63p/message} and {@code /mcp;x=1/message} would skip the filter while still reaching
     * the MCP endpoint.
     */
    private static String routedPath(HttpServletRequest request) {
        var path = ServletRequestPathUtils.parse(request).pathWithinApplication();

        var routed = new StringBuilder();
        for (PathContainer.Element element : path.elements()) {
            if (element instanceof PathContainer.PathSegment segment) {
                routed.append('/').append(segment.valueToMatch());
            }
        }
        return routed.isEmpty() ? "/" : routed.toString();
    }
}
