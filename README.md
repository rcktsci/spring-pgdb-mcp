# PostgreSQL MCP Server

MCP server for connecting to PostgreSQL with read-only and read-write mode support.

Technical stack:

- Java 25
- Spring Boot 4 + Spring AI 2

## Tools

| Tool      | Description                          |
|-----------|--------------------------------------|
| `list`    | List configured database connections |
| `query`   | Execute a SELECT query               |
| `execute` | Execute INSERT/UPDATE/DELETE or DDL  |

## Configuration

### Config file

Create a `config.yml` file:

```yaml
auth-tokens:
  "<token-1>":                               # token string, generate e.g. with `uuidgen`
    - server: staging                        # allowed server names
  "<token-2>":
    - server: staging
    - server: production
      readonly: true                         # optional, overrides the server's readonly

servers:
  - name: "staging"
    host: "staging-env.example.com"
    port: 5432
    username: "staging_username"
    password: "staging_password"
    database: "staging"
    readonly: false
  - name: "production"
    host: "production-env.example.com"
    port: 5432
    username: "production_username"
    password: "production_password"
    database: "production"
    readonly: true
```

### Parameters

| Parameter  | Description                                        |
|------------|----------------------------------------------------|
| `name`     | Unique server name                                 |
| `host`     | PostgreSQL host                                    |
| `port`     | Port (default 5432)                                |
| `username` | Username                                           |
| `password` | Password                                           |
| `database` | Database name                                      |
| `readonly` | `true` — SELECT only, `false` — full access        |

## Authentication

Every MCP request must carry an `Authorization: Bearer <token>` header. Requests without a valid token are rejected with `401 Unauthorized`.

The `auth-tokens` section maps a token to the list of servers the token may access:

- `list` shows only the servers allowed for the token;
- `query` and `execute` reject server names outside the token's list;
- `readonly` on an entry overrides the server's own `readonly` for this token; when omitted, the server's `readonly` applies. `readonly: true` makes the server read-only for the token even if the connection is read-write (`execute` rejected, `query` on a read-only session), `readonly: false` grants writes even if the server is configured readonly. In both directions the restriction is enforced on the database session level, so data-modifying CTEs and multi-statement batches obey it too.

If `auth-tokens` is missing or empty, no client can connect.

## Build

```bash
./mvnw package -DskipTests
```

## Run

### Docker

```bash
docker run -d \
  -v ./config.yml:/application/config.yml:ro \
  -p 2345:8080 \
  ghcr.io/rcktsci/spring-pgdb-mcp:2026-10-29
```

### Docker Compose

Example exposing on port 2345:

```yaml
services:
  mcp-server:
    image: ghcr.io/rcktsci/spring-pgdb-mcp:2026-10-29
    ports:
      - 0.0.0.0:2345:8080/tcp
    volumes:
      - ./config.yml:/application/config.yml:ro
```

## MCP

The server uses MCP SSE transport.

- **MCP endpoint:** `:8080/mcp` (inside container)
- **MCP endpoint:** `http://<IP>:2345/mcp` (published port)

## Connecting tools in external clients (Claude Code, OpenCode, etc.)

```json
{
  "mcpServers": {
    "test_pgdb": {
      "url": "http://<IP>:2345/mcp",
      "headers": {
        "Authorization": "Bearer <your-token>"
      }
    }
  }
}
```
