package se.rocketscien.mcp.springpgdbmcpserver.tests;

import org.junit.jupiter.api.Test;

import se.rocketscien.mcp.springpgdbmcpserver.BaseApplicationTest;

import static org.assertj.core.api.Assertions.assertThat;

class AuthFilterTest extends BaseApplicationTest {

    private static final String INITIALIZE = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "method": "initialize",
              "params": {
                "protocolVersion": "2025-06-18",
                "capabilities": {},
                "clientInfo": {"name": "e2e-test", "version": "0.0.1"}
              }
            }
            """;

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        var response = post(null, INITIALIZE, null);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("WWW-Authenticate")).contains("Bearer");
        assertThat(response.body()).contains("unauthorized");
    }

    @Test
    void requestWithUnknownTokenIsUnauthorized() throws Exception {
        assertThat(post("no-such-token", INITIALIZE, null).statusCode()).isEqualTo(401);
    }

    @Test
    void initializeWithKnownTokenOpensSession() throws Exception {
        var response = post(ALL_TOKEN, INITIALIZE, null);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("mcp-session-id")).isPresent();
        assertThat(response.body()).contains("serverInfo");
    }
}
