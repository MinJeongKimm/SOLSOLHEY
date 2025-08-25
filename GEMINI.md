# Repository Guidelines

## Project Structure & Module Organization
- `backend/solsol/`: Spring Boot (Gradle). Source in `src/main/java`, tests in `src/test/java`, config in `src/main/resources`. Build files: `build.gradle`, `gradlew`.
- `backend/.env`: Active backend environment (hidden). Template: `backend/solsol/env.example` (see also `README-ENV.md`).
- `frontend/`: Vite + Vue 3 + TypeScript. App lives in `frontend/solsol/` (`index.html`, `src/`); static assets in `frontend/public/`.
- `documents/`, `logs/`: Reference and runtime artifacts. Do not commit secrets.

## Build, Test, and Development Commands
- Backend run: `cd backend/solsol && ./gradlew bootRun` — starts API on `http://localhost:8080` with `local` profile.
- Backend test: `./gradlew test` — runs JUnit 5 tests.
- Backend build: `./gradlew build` — creates JAR in `build/libs/`.
- Rate limit check: `cd backend/solsol && ./test-rate-limit.sh` — smoke-test rate limiting.
- Frontend dev: `cd frontend && npm ci && npm run dev` — Vite dev server on `http://localhost:5173`.
- Frontend build: `npm run build` — outputs to `frontend/dist/`.

## Coding Style & Naming Conventions
- Java 17, Spring conventions, package-by-feature (e.g., `auth/`, `mascot/`, `finance/`). Classes `PascalCase`; methods/fields `camelCase`; DTOs end with `Request`/`Response`; controllers end with `Controller`.
- Vue/TS: components `PascalCase.vue` in `components/` or feature folders; pages in `views/`. Shared types in `src/types/`. Keep API clients under `src/api/`.
- Indentation: 4 spaces (Java), 2 spaces (FE). Keep imports organized; no enforced linter—follow existing patterns.

## Testing Guidelines
- Backend: Place tests under `backend/solsol/src/test/java/...` mirroring package paths. File names: `*Tests.java` or `*Test.java`. Run with `./gradlew test`.
- Target: service/business logic and controller behavior; prefer mocking external integrations.
- Frontend: No test runner configured—propose and align before adding tests.

## Commit & Pull Request Guidelines
- Branches: `main` (release), `develop` (integration), `feature/<scope>`.
- Commits (conventional): `feat: add mascot equip API`, `fix: handle JWT expiration`, `docs: update README`.
- PRs: include purpose, linked issues, steps to verify (commands/URLs), and screenshots for UI changes. Keep scope focused; update `env.example`/docs when config changes.

## Security & Configuration Tips
- Copy `backend/solsol/env.example` to `backend/.env` and set: `JWT_SECRET_KEY`, `DB_*`, `CORS_ALLOWED_ORIGINS`, `FINANCE_API_KEY`.
- Profiles: `SPRING_PROFILES_ACTIVE=local|dev|prod`. In prod, disable dev-only surfaces (H2, Swagger) and restrict CORS. Never commit `.env`.

