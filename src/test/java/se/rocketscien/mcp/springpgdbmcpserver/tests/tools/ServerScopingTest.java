package se.rocketscien.mcp.springpgdbmcpserver.tests.tools;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.rocketscien.mcp.springpgdbmcpserver.tests.BaseCallToolTest;

import static org.assertj.core.api.Assertions.assertThat;

class ServerScopingTest extends BaseCallToolTest {

    private static final String TABLE = "agent_notes";

    @BeforeEach
    void setupTable() {
        executeJdbc("DROP TABLE IF EXISTS " + TABLE);
        executeJdbc("CREATE TABLE " + TABLE + " (id serial PRIMARY KEY, note text)");
        executeJdbc("INSERT INTO " + TABLE + " (note) VALUES ('keep-me')");
    }

    @Test
    void listShowsAllServersForFullToken() {
        assertThat(serverNames(listServers(ALL_TOKEN)))
                .containsExactlyInAnyOrder(RW_SERVER, RO_SERVER);
    }

    @Test
    void listShowsOnlyAllowedServersForScopedToken() {
        assertThat(serverNames(listServers(RO_ONLY_TOKEN)))
                .containsExactly(RO_SERVER);
    }

    @Test
    void queryOnAllowedServerReturnsRows() {
        var response = query(RO_ONLY_TOKEN, RO_SERVER, "SELECT note FROM " + TABLE);

        assertThat(getResponseJson(response).get(0).get("note").asText()).isEqualTo("keep-me");
    }

    @Test
    void queryOnServerOutsideScopeIsRejected() {
        var response = query(RO_ONLY_TOKEN, RW_SERVER, "SELECT 1");

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText())
                .contains("not allowed for this token");
    }

    @Test
    void executeOnServerOutsideScopeIsRejected() {
        var response = execute(RO_ONLY_TOKEN, RW_SERVER, "INSERT INTO " + TABLE + " (note) VALUES ('nope')");

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText())
                .contains("not allowed for this token");
    }

    private java.util.List<String> serverNames(String rawResponse) {
        var names = new ArrayList<String>();
        getResponseJson(rawResponse).forEach(row -> names.add(row.get("name").asText()));
        return names;
    }
}
