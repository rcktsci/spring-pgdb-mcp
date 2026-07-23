# PostgreSQL MCP Server

MCP-сервер для подключения к PostgreSQL с поддержкой read-only и read-write режимов.

## Предоставляемые инструменты (tools)

| Инструмент | Описание                                  |
|------------|-------------------------------------------|
| `list`     | Список настроенных подключений            |
| `query`    | Выполнить SELECT запрос                   |
| `execute`  | Выполнить INSERT/UPDATE/DELETE/др. запрос |

## Настройка

### Конфигурация

Создайте файл `config.yml`:

```yaml
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

### Параметры

| Параметр   | Описание                                        |
|------------|-------------------------------------------------|
| `name`     | Уникальное имя сервера                          |
| `host`     | Хост PostgreSQL                                 |
| `port`     | Порт (по умолчанию 5432)                        |
| `username` | Имя пользователя                                |
| `password` | Пароль                                          |
| `database` | Имя БД                                          |
| `readonly` | `true` — только SELECT, `false` — полный доступ |

## Сборка

```bash
./mvnw package -DskipTests
```

## Запуск

### Docker

```bash
docker run -d \
  -v ./config.yml:/application/config.yml:ro \
  -p 2345:8080 \
  ghcr.io/rcktsci/spring-pgdb-mcp:<VERSION>
```

### Docker Compose

Например, развёртывание на порту 2345:

```yaml
services:
  mcp-server:
    image: ghcr.io/rcktsci/spring-pgdb-mcp:<VERSION>
    ports:
      - 0.0.0.0:2345:8080/tcp
    volumes:
      - ./config.yml:/application/config.yml:ro
```

## MCP

Сервер использует MCP SSE transport.

- **MCP endpoint:** `:8080/mcp` (внутри контейнера)
- **MCP endpoint:** `http://<IP>:2345/mcp` (опубликованный порт)

## Подключение инструментов во внешних клиентах (Claude Code, OpenCode, т.п.)

```json
{
  "mcpServers": {
    "test_pgdb": {
      "url": "http://<IP>:2345/mcp"
    }
  }
}
```
