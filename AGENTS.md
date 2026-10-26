# AGENTS.md

## Domain rules (do not break)

- Multi-tenant: every business table carries `organization_id`, and every query filters by the caller's organization. Cross-organization access returns 404.
- Roles are a Java enum stored as text (CLIENT, MANAGER, TECHNICIAN, ORGANIZATION_ADMIN, PLATFORM_ADMIN). One role per user; permissions are fixed in code.
- Auth: Supabase Auth issues the JWT; the backend validates it. Roles, teams and organizations live in our own tables.
- Technicians belong to one team and have no site. Client users see sites through `user_sites`.
- Incident status changes go only through the state machine; reopening, cancelling and putting on hold require a reason. CLOSED and CANCELLED are terminal.
- The work report is editable only while IN_PROGRESS or ON_HOLD. The draft PDF is generated on the fly and never stored; the final PDF is generated once on CLOSED and is immutable.
- Full details: `docs/technical-decisions.md` and `docs/functionality.md`.

## Layout

- Three independent projects in one repo: `incideafrontend/` (React 19 + Vite 8 + TanStack Router), `incideabackend/` (Spring Boot 4.1.2 + Java 21), and `incideainfrastructure/` (Docker Compose files + the Supabase CLI dependency). `supabase/config.toml` configures the local Supabase project.
- There is no root `package.json` or pnpm workspace. Run pnpm only inside the relevant project; root-level workflows go through the `Makefile`.
- `README.md` documents the full architecture and ports. The `Makefile` is the executable source of truth for development, production, and Supabase commands.
- The dev and prod Compose stacks are isolated from each other but share the same Supabase database. The backend joins the external `supabase_network_incidea-app` network, so inside containers the database host is `db:5432`, never `localhost`.

## Commands

- First run: `make setup` (installs the Supabase CLI, creates `incideainfrastructure/.env`), then `make build-dev` (required before `make dev` can start).
- Daily use: `make dev`; logs with `make logs` or `make logs SERVICE=backend`; container shell with `make backend-shell`.
- After editing Java source: `make backend-compile` (devtools restarts the context). After deleting or renaming a Java class: `make backend-clean-compile`; a plain compile leaves stale `.class` files in `target/classes`.
- After changing `pom.xml`, `package.json`, or a lockfile: `make build-dev`.
- Production: `make build-prod` (frontend on 8081, backend on 8082).
- Use the Make targets instead of raw `docker compose up`: the Supabase CLI recreates its Docker network, and the Makefile uses `--force-recreate` to avoid stale network references.

## Conventions

- Language: code, identifiers, comments, commit messages and docs are in English. User-facing text is in Spanish. Reply to the developer in Spanish.
- Functions have a single purpose and stay under 20 lines.
- Java: classes in PascalCase, methods and variables in camelCase, constants in UPPER_SNAKE_CASE, packages in lowercase. TypeScript: components in PascalCase, everything else in camelCase.
- Backend packages are organized by feature (`incident/`, `user/`, `organization/`, `team/`, `common/`), not by layer.
- Database: snake_case plural table names, UUID primary keys, timestamps as `timestamptz` in UTC. API dates use ISO-8601.
- Never expose JPA entities through the API: use DTOs. Validate input with Bean Validation and return errors in Spring's `ProblemDetail` format.
- Nothing is physically deleted: users, teams and incidents are deactivated or cancelled.
- Authorization is always enforced in the backend. Frontend checks are only UX.

## Limits

- Never commit secrets (`.env`, `service_role` keys, OAuth client secrets).
- Never edit a Flyway migration that has already been applied; create a new one.
- Never edit `src/routeTree.gen.ts` by hand.
- Never run destructive commands (`make clean`, `make supabase-reset`) without asking first.
- Never commit generated code that you have not reviewed and cannot explain.
- Never query business data without filtering by the caller's organization.
- Do not commit, push or open pull requests unless asked.
- Do not add or upgrade dependencies without asking and explaining why.
- Never use `ddl-auto=update` or `create`, and never bypass checks (`--no-verify`, disabling CI).
- Never log tokens, passwords or personal data.

## Verification

- Frontend has no test runner. Verify with:
  ```bash
  cd incideafrontend
  pnpm lint
  pnpm exec tsc --noEmit   # `pnpm build` alone does NOT typecheck
  pnpm build
  ```
  `pnpm check` only runs Prettier; it is not a typecheck.
- Backend tests need a real PostgreSQL database. Docker builds use `-DskipTests`, so `make build-prod` does not run them:
  ```bash
  make supabase-start
  cd incideabackend
  SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:54322/postgres \
  SPRING_DATASOURCE_USERNAME=postgres \
  SPRING_DATASOURCE_PASSWORD=postgres \
  ./mvnw test
  ```
  Single test: add `-Dtest=IncideabackendApplicationTests` (or `-Dtest=IncideabackendApplicationTests#contextLoads`).
- Backend health check: `curl -fsS http://localhost:8080/actuator/health`.

## Gotchas

- Database credentials live in `incideainfrastructure/.env` (ignored by Git); `make setup` copies `.env.example`. Never commit `service_role` keys or OAuth client secrets.
- Flyway and Hibernate use the `app` schema (see `incideabackend/src/main/resources/application.properties`). Add migrations under `incideabackend/src/main/resources/db/migration`.
- The frontend always calls `/api`: Vite proxies to `localhost:8080` locally and to `backend:8080` in Compose, and nginx proxies in production. Do not hardcode backend origins or add CORS workarounds.
- TanStack Router uses file-based routing: add pages under `incideafrontend/src/routes`; `src/routeTree.gen.ts` is generated by the Vite plugin and is committed, so do not edit it by hand.
- pnpm 12 reads build-script allowlists from `pnpm-workspace.yaml` (`allowBuilds`), not from the `pnpm` field in `package.json`. Node/pnpm versions are pinned by `packageManager`/`engines` and by the Docker images (Node 24.22.1, pnpm 12.6.0).
- `incideabackend/pom.xml` is authoritative for tool versions (currently Spring Boot 4.1.2, Java 21); the README may lag behind it.
- `make clean` removes only app containers, images, and volumes; `make supabase-reset` deletes all local Supabase data. Use `CONFIRM=1` only when the destructive action is intentional.

## How to work with me

Act as a senior engineer and technical lead for this project, not as a code generator.

- Before implementing, check the request against the domain rules and the docs. If it conflicts with them, say so and propose alternatives.
- When there are several options, present them with trade-offs and give a clear recommendation. Do not just agree: challenge weak decisions, with reasons.
- Flag risks early: data isolation between organizations, security, performance, maintainability, and scope creep beyond the MVP.
- Prefer the simplest solution that meets the current need. Call out over-engineering.
- The developer is a junior who wants to understand, not just receive code. Explain the "why" of non-trivial decisions in two or three lines.
- If a requirement is ambiguous, ask one or two focused questions. Otherwise, state your assumptions and proceed.
- Keep changes small and scoped to the issue. Suggest follow-up issues for anything out of scope instead of fixing it silently.
- Point out mistakes in existing code or docs, including your own earlier suggestions.

## Git workflow

- `main` is protected and deployed: no direct pushes. It only receives merges from `dev`, using a merge commit (never squash).
- `dev` is the default branch and the integration branch for both frontend and backend. Direct pushes are not allowed.
- Work branches are created from the GitHub issue and keep the name GitHub generates (for example `12-add-login-endpoint`). Do not rename them.
- Work branches are merged into `dev` through a pull request with squash. Each PR references its issue with `Closes #N`.
- Keep work branches short-lived. Update `dev` before creating a branch and bring the latest `dev` into your branch before opening the PR.
- Issue titles are written in English, short and in the imperative, because the branch name comes from them.
- Commit messages are in English and follow Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`, `test:`).
- Never push to `main` or `dev`, and never force-push a shared branch.

## Glossary

- Organization: the maintenance company that uses Incidea (the tenant).
- Client: a company that reports incidents to an organization. It has sites.
- Site: a client's location where an incident happens.
- Team: a group of technicians that works for an organization.
- MANAGER works for the organization. CLIENT works for the customer company.

## Scope

- Current focus: see `ROADMAP.md`. Do not implement features from later sprints or from the backlog (AI, emails, comments) unless asked.

## Testing

- New behavior ships with tests. Priority: authorization and organization isolation, status transitions, and the report/PDF rules.
- A bug fix starts with a test that reproduces it.
- Backend: JUnit 5 and Mockito for units, Testcontainers (PostgreSQL) for integration, Spring Security Test for roles.
- Frontend: Vitest and React Testing Library, MSW for the API, Playwright for the main E2E flows.
- Never disable, skip or weaken a test to make it pass.
