package se.rocketscien.mcp.springpgdbmcpserver.tests.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.rocketscien.mcp.springpgdbmcpserver.tests.BaseCallToolTest;

import static org.assertj.core.api.Assertions.assertThat;

/** A token entry with {@code readonly: true} on a read-write server. */
class TokenReadonlyTest extends BaseCallToolTest {

    private static final String TABLE = "agent_notes";

    @BeforeEach
    void setupTable() {
        executeJdbc("DROP TABLE IF EXISTS " + TABLE);
        executeJdbc("CREATE TABLE " + TABLE + " (id serial PRIMARY KEY, note text)");
        executeJdbc("INSERT INTO " + TABLE + " (note) VALUES ('keep-me')");
    }

    @Test
    void selectWorksOnTokenReadonlyServer() {
        var response = query(RO_ON_RW_TOKEN, RW_SERVER, "SELECT note FROM " + TABLE);

        assertThat(getResponseJson(response).get(0).get("note").asText()).isEqualTo("keep-me");
    }

    @Test
    void executeIsRejectedForTokenReadonlyServer() {
        var response = execute(RO_ON_RW_TOKEN, RW_SERVER, "INSERT INTO " + TABLE + " (note) VALUES ('nope')");

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText()).contains("readonly");
        assertThat(rowCount(TABLE)).isEqualTo(1);
    }

    @Test
    void queryToolCannotDeleteThroughDataModifyingCteOnTokenReadonlyServer() {
        var response = query(RO_ON_RW_TOKEN, RW_SERVER,
                "WITH d AS (DELETE FROM " + TABLE + " RETURNING *) SELECT * FROM d");

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText()).contains("read-only");
        assertThat(rowCount(TABLE)).isEqualTo(1);
    }

    @Test
    void queryToolCannotExecuteMultiStatementBatchOnTokenReadonlyServer() {
        var response = query(RO_ON_RW_TOKEN, RW_SERVER, "SELECT 1; DELETE FROM " + TABLE);

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText()).contains("read-only");
        assertThat(rowCount(TABLE)).isEqualTo(1);
    }
}
