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
  "0d3aa0f9-5b1e-4a1f-9d0e-2f8c1b7a6c55":   # token string (e.g. UUID)
    - server: staging                        # allowed server names
  "f81d4fae-7dec-11d0-a765-00a0c91e6bf6":
    - server: staging
    - server: production
      readonly: true                         # optional, default false

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
- `readonly: true` on an entry makes that server read-only for the token even if the connection itself is read-write: `execute` is rejected and `query` runs on a read-only session, so data-modifying CTEs and multi-statement batches fail on the database level.

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
  ghcr.io/rcktsci/spring-pgdb-mcp:2026-10-28
```

### Docker Compose

Example exposing on port 2345:

```yaml
services:
  mcp-server:
    image: ghcr.io/rcktsci/spring-pgdb-mcp:2026-10-28
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
        "Authorization": "Bearer 0d3aa0f9-5b1e-4a1f-9d0e-2f8c1b7a6c55"
      }
    }
  }
}
```
