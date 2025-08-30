# Repository Guidelines

## Project Structure & Module Organization
- Backend: `backend/solsol/` (Spring Boot). Code in `src/main/java`, tests in `src/test/java`, config in `src/main/resources` (`application.yml`).
- DB migrations: `backend/solsol/src/main/resources/db/migration` (Flyway).
- Env: active env is `backend/.env` (hidden). Template: `backend/solsol/env.example`.
- Frontend: `frontend/` (Vite + Vue 3 + TS). App under `frontend/solsol/` (`index.html`, `src/`); static assets in `frontend/public/`.
- Docs/Logs: `documents/`, `logs/` for references and runtime artifacts.

## Build, Test, and Development Commands
- Backend run: `cd backend/solsol && ./gradlew bootRun` (defaults to `local`).
- Force profile: `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`.
- Backend test/build: `./gradlew test`, `./gradlew build` (JAR in `build/libs/`).
- Rate limit smoke: `cd backend/solsol && ./test-rate-limit.sh`.
- Frontend dev/build: `cd frontend && npm ci && npm run dev` (http://localhost:5173), `npm run build` → `frontend/dist/`.
- Flyway tip (local only): if checksum mismatch occurs, remove `backend/solsol/.localdb` or run `./gradlew flywayRepair` and restart. Never edit applied migrations; add a new `VXX__*.sql` (e.g., `V36__deprecate_blue_background.sql`).

## Coding Style & Naming Conventions
- Java 17 + Spring: 4-space indent, package-by-feature (`auth/`, `finance/`, `mascot/`). Classes `PascalCase`; fields/methods `camelCase`; DTOs `*Request`/`*Response`; controllers `*Controller`.
- Vue/TS: 2-space indent. Components `PascalCase.vue`; pages under `views/`; shared types in `src/types/`; API clients in `src/api/`.

## Testing Guidelines
- Backend: JUnit 5. Place tests in `backend/solsol/src/test/java/...` mirroring packages. Names: `*Test.java` or `*Tests.java`. Run `./gradlew test`.
- Focus: service/business logic and controller behavior. Mock external integrations.

## Commit & Pull Request Guidelines
- Branches: `main`(release), `develop`(integration), `feature/<scope>`.
- Conventional commits: e.g., `feat: add mascot equip API`, `fix: handle JWT expiration`.
- PRs: include purpose, linked issues, verification steps (commands/URLs), and screenshots for UI changes. Update env/docs when config changes.

## Security, Config & Environments
- Env: copy `backend/solsol/env.example` → `backend/.env`. Never commit secrets.
- Profiles: `local`(H2, 개인 개발) and `dev`(PostgreSQL, 공용 원격 DB). Switch via `SPRING_PROFILES_ACTIVE`.
- CORS: restrict `CORS_ALLOWED_ORIGINS` to actual domains.
- Docker (dev): run backend container with `--env-file backend/.env`; prefer shared remote Postgres for team consistency.
- Asset note: `frontend/public/backgrounds/base/bg_blue.png` is deprecated and must not be referenced.
