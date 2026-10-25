# WebSocket Chat Service 💬🏨

Real-time chat between hotel staff and in-room guests, built on Spring Boot, STOMP over WebSocket (with a SockJS fallback) and a multi-tenant MySQL setup: one database per customer, resolved from a common registry database.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## ✨ Features

- Guest view: one conversation with the hotel, opened from a link (`guest=1`)
- Staff view: list of active guest chats with search, unread badges, sound notifications, and assignment back to all staff (`guest=0`)
- Chats are assigned automatically to the first staff member who answers
- Message status: sending, queued while offline, delivered, seen, failed
- Message history with "show previous messages", grouped by day
- Emojis, and image messages (up to 5 images, 5 MB each, with an optional caption)
- Automatic reconnection with backoff; messages typed while offline are sent after reconnecting
- Responsive layout, light and dark themes, keyboard accessible

## 🛠️ Tech stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 2.7.18 (Java 8), Spring WebSocket + STOMP simple broker, Spring Data JPA / Hibernate 5.6 |
| Database | MySQL 8, per-tenant HikariCP pools behind an `AbstractRoutingDataSource` |
| Frontend | Vanilla JS, SockJS 1.6, STOMP.js 2.3, Font Awesome 4.7 (no jQuery or Bootstrap) |
| Runtime | WAR on Tomcat 9 (Docker image provided) |

## 📁 Project structure

```
src/main/java/com/ts/messenger/
├── ManagmentApplication.java        Spring Boot entry point, tenant routing DataSource
├── authentication/                  Credentials (config + env), Interceptor (token validation, hotel checks)
├── communication/Message.java       STOMP message model + validation
├── config/                          WebSocket, security headers, tenant cleanup filter, error handler
├── controller/                      APIsController (REST), MessagingController (STOMP)
├── entities/  repositories/  services/
├── multi/tenant/                    per-request tenant (customer) database routing
├── utils/                           AES-GCM encryption, secure tokens, helpers
└── ws/StompAuthChannelInterceptor   CONNECT / SUBSCRIBE / SEND authorization
src/main/webapp/                     index.jsp, scripts/, content/styles/site.css
src/main/databaseScripts/            messenger tables + triggers
docker/                              local demo stack (MySQL init, mock auth service)
```

## 🚀 Run with Docker (local demo)

The demo stack starts MySQL (with a demo tenant), a **mock** authentication service, and the messenger.

```bash
cp .env.example .env            # then put real random values in it, e.g.:
#   MYSQL_ROOT_PASSWORD=$(openssl rand -hex 24)
#   MESSENGER_DB_PASSWORD=$(openssl rand -hex 24)
#   MESSENGER_ENCRYPTION_KEY=$(openssl rand -base64 32)
docker compose up -d --build
```

Then open:

- Staff: http://localhost:8080/messenger/?h=1&cn=demohotel&guest=0&session=staff1
- Guest: http://localhost:8080/messenger/?h=1&cn=demohotel&guest=1&session=john&roomNumber=101

The ports are bound to `127.0.0.1` only. `docker compose down -v` removes the demo data.

> `docker/mock-auth` and the demo seed data are for local testing only. In production the messenger talks to your real authentication service and databases.

## 🔧 Build and deploy

```bash
# one time: install the private CustomerIdentificator dependency
mvn install:install-file -Dfile=CustomerIdentificator-0.0.2-SNAPSHOT.jar \
    -DgroupId=NCI -DartifactId=CustomerIdentificator -Dversion=0.0.2-SNAPSHOT -Dpackaging=jar

mvn clean package               # runs the unit tests, produces target/messenger.war
docker build -t messenger .     # or deploy messenger.war to Tomcat 9 (not Tomcat 10: javax.*)
```

### Configuration (environment variables)

| Variable | Required | Description |
|---|---|---|
| `MESSENGER_ENCRYPTION_KEY` | **yes** | Base64 AES-256 key (`openssl rand -base64 32`). The app refuses to start without it. |
| `COMMON_DB_URL`, `COMMON_DB_USER`, `COMMON_DB_PASSWORD`, `COMMON_DB_CATALOG`, `COMMON_DB_DRIVER` | yes | Common (tenant registry) database |
| `AUTH_VALIDATE_URL` | yes | Token validation endpoint of the authentication service |
| `MESSENGER_AUTH_SERVICE_URL` | yes | Base URL of the auth service as seen by browsers (`getCustomerByName`, `token/generateToken`); also added to the page CSP. Falls back to `AuthServiceUrl` in `web.xml`. |
| `MESSENGER_ALLOWED_ORIGINS` | no | Comma-separated origin patterns allowed to open the WebSocket from another site. Default: same origin only. |
| `MESSENGER_FRAME_ANCESTORS` | no | CSP `frame-ancestors` (who may embed the chat in an iframe). Default: `'self'`. |
| `UPLOAD_IMAGES_PATH` | no | Storage path for images, `#customerName#` is replaced. Default `/tsl/midd_data/portal/#customerName#/img/messenger` |
| `TENANT_POOL_MAX_SIZE` | no | Max connections per tenant pool (default 5) |
| `DEFAULT_CUSTOMER_ID` | no | Tenant used when none is bound to the request |
| `MESSENGER_DEBUG` | no | Verbose logs (message content and tokens are never logged) |
| `MESSENGER_API_DOCS_ENABLED` | no | Enables OpenAPI docs / Swagger UI (off by default) |

## 🔌 Protocol

### REST (`/messenger/api/…`)

Every call needs the `authorization: Bearer <token>`, `customerId` and `hotelId` headers. Calls on a specific chat also need `chatSecurityToken`.

| Method | Path | Chat token | Purpose |
|---|---|---|---|
| GET | `hotel/guests?hotelUsername=` | – | Active guest chats for a staff member |
| GET | `hotel/guest/logIn?guestUsername=&roomNumber=&roomTypeCode=` | – | Opens or resumes the chat of a room and returns its `securityToken` |
| GET | `hotel/guest/logOut?guestUsername=&roomNumber=` | ✔ | Closes the chat; the token becomes invalid |
| GET | `hotel/guest/{guest}/{room}/messages/{fromRow}/{toRow}` | ✔ | History (page size capped at 5000) |
| GET | `hotel/guest/assign/{guest}/{room}` | ✔ | Makes the chat available to all staff again |
| GET | `chat/status/change?roomNumber=&guestUsername=|hotelUsername=` | ✔ | Marks received messages as SEEN |
| POST | `upload/image` (multipart `image_0..4`, header `customerName`) | – | Stores images, returns their URLs |

### STOMP (`/messenger/sendmsg`, SockJS)

| Frame | Destination | Required headers |
|---|---|---|
| CONNECT | – | `authorization`, `customerId` |
| SUBSCRIBE | `/topic/private/{customerId}.{hotelId}.{room}.{guest}` | `chatSecurityToken`, `hotelId`, `roomNumber`, `guestUsername` |
| SUBSCRIBE | `/topic/private/{hotelId}` (staff: `refresh_guest_list` events) | – |
| SUBSCRIBE | `/user/queue/errors` (errors for this session only) | – |
| SEND | `/app/sendmsg` | JSON message (see `Message.java`) |

Clients can't publish directly to `/topic/**` or `/queue/**`; the server drops those frames.

## 🔒 Security

- **Authentication**: bearer tokens are validated against the authentication service, with timeouts, on every REST call and on the STOMP `CONNECT`. The tenant (customer database) comes from the authenticated token, and messages for another customer are rejected.
- **Per-chat authorization**: 256-bit random chat tokens (`SecureRandom`) are required to subscribe to a chat, read its history, send to it, log out, assign it or mark it as seen. Tokens are compared in constant time, are never included in broadcast messages, and stop working at logout.
- **Hotel senders** must be existing staff accounts (`utilisateur`); a chat already assigned to another staff member is protected.
- **Encryption at rest**: message content uses AES-256-GCM with a random IV and a key from the environment. Rows written by older versions (AES/ECB) are still readable but are never written again.
- **No data API exposure**: Spring Data REST was removed (it published every table, including users and chat tokens). Actuator exposes only `health`; API docs are off by default.
- **Input validation**: message size and format limits; image messages must be plain `http(s)` URLs; room numbers can't contain `.`; uploads are decoded with dimension limits (decompression bombs), re-encoded as PNG with unique names, and the storage path is checked against the authenticated customer (path traversal).
- **Browser hardening**: everything is rendered with `textContent` (no HTML injection), there are no inline scripts, and pages carry a strict Content-Security-Policy, Subresource Integrity on CDN assets, `Referrer-Policy: no-referrer` (links carry session parameters), `nosniff`, `frame-ancestors`, and HSTS over HTTPS.
- **Operations**: no secrets in the repository (`.env` is git-ignored), least-privilege DB user in the demo, non-root container, Tomcat shutdown port and error details disabled, generic API error messages (details only in server logs).

### Known limitations (outside this service)

- The guest `session` value in the link acts as the guest's credential for `logIn`, and `customerKey` is handed to browsers by `getCustomerByName`. Both come from the external portal and auth service. Signed, short-lived links issued by the portal would close this gap.
- Staff identity (`session=staff1`) is not authenticated by this service, because the auth token identifies the customer, not the user. Staff access should come from a real user login (e.g. an SSO token with the username claim).
- The STOMP simple broker is in-memory; scaling horizontally needs a full broker relay (RabbitMQ / ActiveMQ) and sticky sessions.

## 🧪 Tests

```bash
mvn test
```

Unit tests cover encryption (round trip, tampering, legacy rows), message validation and token generation.

## 📝 License

MIT, see [LICENSE](LICENSE).

## 👨‍💻 Author

**Toufic Sleiman** · [GitHub](https://github.com/touficSl) · [LinkedIn](https://www.linkedin.com/in/toufic-sleiman/) · touficsleiman.uae@gmail.com
