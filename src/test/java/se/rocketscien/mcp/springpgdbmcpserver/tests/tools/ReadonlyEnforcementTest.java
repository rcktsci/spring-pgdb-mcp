package se.rocketscien.mcp.springpgdbmcpserver.tests.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.rocketscien.mcp.springpgdbmcpserver.tests.BaseCallToolTest;

import static org.assertj.core.api.Assertions.assertThat;

class ReadonlyEnforcementTest extends BaseCallToolTest {

    private static final String TABLE = "agent_notes";

    @BeforeEach
    void setupTable() {
        executeJdbc("DROP TABLE IF EXISTS " + TABLE);
        executeJdbc("CREATE TABLE " + TABLE + " (id serial PRIMARY KEY, note text)");
        executeJdbc("INSERT INTO " + TABLE + " (note) VALUES ('keep-me')");
    }

    @Test
    void selectWorksOnReadonlyServer() {
        var response = query(ALL_TOKEN, RO_SERVER, "SELECT note FROM " + TABLE);

        assertThat(getResponseJson(response).get(0).get("note").asText()).isEqualTo("keep-me");
    }

    @Test
    void executeIsRejectedOnReadonlyServer() {
        var response = execute(ALL_TOKEN, RO_SERVER, "INSERT INTO " + TABLE + " (note) VALUES ('nope')");

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText()).contains("readonly");
        assertThat(rowCount(TABLE)).isEqualTo(1);
    }

    @Test
    void queryToolCannotDeleteThroughDataModifyingCte() {
        var response = query(ALL_TOKEN, RO_SERVER,
                "WITH d AS (DELETE FROM " + TABLE + " RETURNING *) SELECT * FROM d");

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText()).contains("read-only");
        assertThat(rowCount(TABLE)).isEqualTo(1);
    }

    @Test
    void queryToolCannotExecuteMultiStatementBatch() {
        var response = query(ALL_TOKEN, RO_SERVER, "SELECT 1; DELETE FROM " + TABLE);

        assertThat(unwrapPayload(getResponseJson(response)).get("error").asText()).contains("read-only");
        assertThat(rowCount(TABLE)).isEqualTo(1);
    }

    @Test
    void executeWorksOnReadWriteServer() {
        var response = execute(ALL_TOKEN, RW_SERVER, "INSERT INTO " + TABLE + " (note) VALUES ('written')");

        assertThat(unwrapPayload(getResponseJson(response)).get("affectedRows").asInt()).isEqualTo(1);
        assertThat(rowCount(TABLE)).isEqualTo(2);
    }
}
