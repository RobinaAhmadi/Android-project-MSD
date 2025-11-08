## Auth Server

Simple Ktor + SQLite backend that exposes REST endpoints for registering and logging in.

### Endpoints

| Method | Path             | Body                                     | Description                 |
|--------|------------------|------------------------------------------|-----------------------------|
| POST   | `/api/auth/register` | `{ "email": "", "password": "", "displayName": "" }` | Creates a user (password >= 6 chars). |
| POST   | `/api/auth/login`    | `{ "email": "", "password": "" }`     | Returns JWT + profile info. |
| GET    | `/api/auth/me`       | `Authorization: Bearer <token>`       | Validates token, returns user id. |

All responses (success or error) are simple JSON payloads. Passwords are stored hashed with BCrypt.

### Running locally

```bash
./gradlew :server:run
```

The service listens on `http://0.0.0.0:8080` (for Android emulators use `http://10.0.2.2:8080`).

SQLite file `server-data.db` is generated at the project root and already ignored via `.gitignore`.
