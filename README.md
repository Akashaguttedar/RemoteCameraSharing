# Remote Camera Sharing System

A Java 17 / Spring Boot 3.3 Maven application with a browser participant page, a separate viewer page, short lived sessions, and WebRTC peer to peer media. The participant page intentionally has no video element or local preview. Camera access begins only after the participant clicks **Allow Camera** and grants browser permission.

## Run in Eclipse

1. Install a JDK 17 and Eclipse IDE for Java Developers with Maven support.
2. Extract the ZIP, then choose **File → Import → Maven → Existing Maven Projects**.
3. Select the extracted `RemoteCameraSharing` folder and finish the import.
4. Run `com.example.remotecamera.RemoteCameraApplication` as a Java application. Or from a terminal run `mvn spring-boot:run` in this folder.
5. Open `http://localhost:8080/`. Create a session, send the participant link to the person who agreed to share, and keep the viewer link private.

Sessions expire after one hour. Restarting the application clears all sessions because the sample stores them in memory.

## Deployment

Camera access requires a secure context: use HTTPS in deployed environments (localhost is permitted by browsers). WebSocket signaling will use WSS automatically when the page is HTTPS. Set `ALLOWED_ORIGINS` to a comma separated list of exact trusted browser origins if the pages are served behind a different hostname or proxy. Keep the app and WebSocket route behind TLS and configure reverse proxies to pass WebSocket upgrades.

The sample configures a public STUN server for initial connectivity. Some NATs and corporate networks require TURN; deploy an authenticated TURN service and add its `iceServers` entry in both `src/main/resources/static/js/participant.js` and `viewer.js`. Do not put long lived TURN credentials in public JavaScript; use short lived credentials from a backend service.

## Security and limitations

- Session IDs are random UUIDs; participant and viewer access use separate cryptographically random 256 bit bearer tokens. Treat each link as a secret. Anyone holding the viewer link can view the stream.
- Signaling checks the role token, allows one active socket per role per session, limits message size, and relays only SDP offer/answer and ICE candidate messages within the matching session. Media itself is peer to peer and does not pass through Spring.
- Session state is in process memory. This is a single instance demo: it has no durable store, multi instance coordination, account system, rate limiting, audit log, TURN service, or operational hardening. HTTPS, secret link handling, deployment testing, abuse controls, and secure TURN credentials are required before real deployment. It is not production ready.
- Closing the participant tab stops local tracks. The Stop Sharing button stops tracks, closes the peer connection, and closes signaling. Session deletion is available through the API using the viewer token.

## API

`POST /api/sessions` creates a session and returns `sessionId`, `participantUrl`, `viewerUrl`, and `expiresAt`. `DELETE /api/sessions/{id}?token=<viewer-token>` revokes a session. WebSocket signaling is at `/ws/signaling?session=<id>&token=<role-token>`.

## Build and tests

Run `mvn test` and `mvn package`. The JAR is written to `target/remote-camera-sharing-1.0.0.jar`.
