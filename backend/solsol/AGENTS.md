# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/solsolhey/`: application code by domain (`auth`, `user`, `challenge`, `ranking`, `friend`, `point`, `finance`, `share`, `common`, `config`).
- `src/main/resources/`: configuration (`application.yml`) and assets (e.g., `db/init-postgresql.sql`).
- `.localdb/`: H2 files for local development.
- Build files: `build.gradle`, `settings.gradle`, `gradlew*`.
- Environment: `env.example` → copy to `.env` at repo root.

## Build, Test, and Development Commands
- Run locally: `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`
  - Starts the Spring Boot app on `:8080` (see `/health`).
- Build: `./gradlew clean build`
  - Produces a runnable jar under `build/libs`.
- Tests: `./gradlew test`
  - Runs JUnit 5 tests (Spring Boot test starter).
- Rate limit check: `./test-rate-limit.sh`
  - Requires the app running; hits `/api/v1/test/rate-limit/*`.

## Coding Style & Naming Conventions
- Language: Java 17, Spring Boot 3, Lombok. Use annotation processing in IDE.
- Indentation: 4 spaces; one public class per file; package prefix `com.solsolhey.<domain>`.
- Naming: `*Controller`, `*Service`/`*ServiceImpl`, `*Repository`, entities in singular (`User`, `Contest`), DTOs as `*Request`/`*Response`.
- REST: group endpoints under `/api/v1/...`; reuse `ApiResponse` for payloads.
- Formatting: keep imports organized; prefer final immutability; use Lombok getters/builders where present.

## Testing Guidelines
- Framework: JUnit 5 with Spring Boot Test and Security Test.
- Location: `src/test/java/...` mirroring main packages.
- Naming: `ClassNameTest` for unit tests; `*IT` acceptable for integration.
- Tips: use `@SpringBootTest` for integration, `@WebMvcTest` for MVC slices, and mock repositories/services where practical.

## Commit & Pull Request Guidelines
- Commits: use short, typed subjects (seen in history: `Feat:`, `Fix:`, `Chore:`, `Refactor:`).
  - Example: `Fix: make MascotController param optional`.
- PRs: include summary, linked issues, testing steps (curl/HTTP examples), and screenshots/logs when UI/API behavior changes.
- CI gate: ensure `./gradlew build` passes locally before opening PR.

## Security & Configuration Tips
- Copy `env.example` to `.env` and never commit secrets.
- Set strong `JWT_SECRET_KEY`; restrict `CORS_ALLOWED_ORIGINS`.
- Profiles: `local` uses H2 (`.localdb`), `dev` uses PostgreSQL (see `application.yml`).
- Quick check: GET `http://localhost:8080/health` returns service status and seeds CSRF cookie.
