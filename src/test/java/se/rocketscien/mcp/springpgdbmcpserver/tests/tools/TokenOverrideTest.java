package se.rocketscien.mcp.springpgdbmcpserver.tests.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.rocketscien.mcp.springpgdbmcpserver.tests.BaseCallToolTest;

import static org.assertj.core.api.Assertions.assertThat;

/** A token entry with an explicit {@code readonly: false} overriding a read-only server. */
class TokenOverrideTest extends BaseCallToolTest {

    private static final String TABLE = "agent_notes";

    @BeforeEach
    void setupTable() {
        executeJdbc("DROP TABLE IF EXISTS " + TABLE);
        executeJdbc("CREATE TABLE " + TABLE + " (id serial PRIMARY KEY, note text)");
        executeJdbc("INSERT INTO " + TABLE + " (note) VALUES ('keep-me')");
    }

    @Test
    void executeIsAllowedWhenTokenOverridesServerReadonly() {
        var response = execute(RW_ON_RO_TOKEN, RO_SERVER, "INSERT INTO " + TABLE + " (note) VALUES ('allowed')");

        assertThat(unwrapPayload(getResponseJson(response)).get("affectedRows").asInt()).isEqualTo(1);
        assertThat(rowCount(TABLE)).isEqualTo(2);
    }

    @Test
    void queryToolDataModifyingCteIsAllowedWhenTokenOverridesServerReadonly() {
        var response = query(RW_ON_RO_TOKEN, RO_SERVER,
                "WITH d AS (DELETE FROM " + TABLE + " RETURNING *) SELECT * FROM d");

        assertThat(getResponseJson(response).get(0).get("note").asText()).isEqualTo("keep-me");
        assertThat(rowCount(TABLE)).isEqualTo(0);
    }
}
