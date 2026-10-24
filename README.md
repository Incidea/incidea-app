# Incidea App

Incidea is a monorepo with a **React frontend**, a **Spring Boot backend**, and a
local **Supabase** stack. Everything runs with Docker Compose and the common
workflows are wrapped in the [`Makefile`](./Makefile).

The Supabase stack provides PostgreSQL, Auth (including OAuth providers),
Storage, Realtime and Studio for local development. The backend talks to it
through Docker's internal network and keeps its own schema (`app`) managed by
Flyway.

## Architecture

```text
HOST (Docker Desktop / WSL)
│
├── Supabase stack (managed by the Supabase CLI, 12 containers)
│   ├── Kong / API ......... localhost:54321  -> 8000
│   ├── PostgreSQL ......... localhost:54322  -> 5432
│   ├── Studio ............. localhost:54323  -> 3000
│   ├── Mailpit ............ localhost:54324  -> 8025
│   ├── Analytics .......... localhost:54327  -> 4000
│   └── auth, rest, storage, realtime, pg-meta, edge-runtime, vector
│       └── network: supabase_network_incidea-app
│
├── Compose project "incidea-dev"  (network: incidea-dev_default)
│   ├── backend (8080) ── default + supabase ──> db:5432
│   └── react   (3000) ── default ──/api──> backend:8080
│
└── Compose project "incidea-prod" (network: incidea-prod_default)
    ├── backend-prod (8082) ── default + supabase ──> db:5432
    └── react-prod   (8081) ── default ──/api──> backend:8080 (nginx)
```

The backend joins the external Supabase network so it can resolve the database
as `db:5432`. It never uses `localhost` for the database: inside a container,
`localhost` is the backend container itself.

### Compose projects

| Project | Services | Purpose |
| --- | --- | --- |
| `incidea-dev` | `react`, `backend` | Development with Vite HMR and Spring Boot devtools |
| `incidea-prod` | `react-prod`, `backend-prod` | Production-like build (nginx + fat JAR) |
| `incidea-app` | 12 `supabase_*` containers | Local Supabase stack, created by the CLI |

The two app projects are isolated from each other (separate networks and
containers), but **both connect to the same Supabase database**.

## Repository layout

```text
.
├── incideafrontend/            # React 19 + Vite 8 + TanStack Router
│   ├── Dockerfile              # Production: build + nginx
│   ├── Dockerfile.dev          # Development: Vite dev server
│   └── nginx.conf              # SPA fallback, caching, gzip, /api proxy
├── incideabackend/             # Spring Boot 4.1.1 + Java 21
│   ├── Dockerfile              # Production: Maven build + JRE alpine
│   ├── Dockerfile.dev          # Development: spring-boot:run + devtools
│   └── pom.xml
├── incideainfraestructure/     # Docker Compose and Supabase CLI
│   ├── compose.dev.yaml
│   ├── compose.yaml
│   ├── .env.example
│   ├── package.json            # Supabase CLI dev dependency
│   └── pnpm-lock.yaml
├── supabase/
│   └── config.toml             # Local Supabase project configuration
├── Makefile
└── README.md
```

## Requirements

| Requirement | Details |
| --- | --- |
| **Docker Desktop** | Includes Docker Compose v2 (`docker compose`). On Windows, use the WSL 2 backend. |
| **GNU Make** | Optional, but every command in this README uses it. |
| **pnpm 12** | Only to install the Supabase CLI inside `incideainfraestructure`. |
| **Node.js 24 and JDK 21** | Only needed for the "without Docker" workflow. Images already include them. |
| **Free ports** | 3000, 8080, 8081, 8082, 54321, 54322, 54323, 54324, 54327. |

## Quick start

```bash
# 1. Install the Supabase CLI and create .env from .env.example
make setup

# 2. Build the images and start Supabase + the development stack
#    The first build downloads Maven/pnpm dependencies and can take a few minutes.
make build-dev

# 3. Check what is running and where
make status
```

| Service | URL |
| --- | --- |
| Frontend (dev) | <http://localhost:3000> |
| Backend (dev) | <http://localhost:8080> |
| Backend health | <http://localhost:8080/actuator/health> |
| Supabase Studio | <http://localhost:54323> |
| Supabase API | <http://localhost:54321> |
| PostgreSQL | `postgresql://postgres:postgres@localhost:54322/postgres` |

After the first build, use `make dev` for day-to-day work. It starts Supabase
if needed and the development stack without rebuilding anything.

## Make commands

### Setup

| Command | What it does |
| --- | --- |
| `make setup` | Checks Docker, installs the Supabase CLI with pnpm (`pnpm install --frozen-lockfile`) and creates `incideainfraestructure/.env` from `.env.example` if it does not exist. |

### Development

| Command | What it does |
| --- | --- |
| `make dev` | Starts Supabase if it is not running and brings up the development stack with `docker compose up -d --wait`. Fails with a clear message if the dev images do not exist yet. |
| `make build-dev` | Rebuilds the development images and starts the stack. Use it the first time, after changing `pom.xml`/`package.json`, or after `make clean`. |
| `make logs` | Follows the development logs. Use `make logs SERVICE=backend` (or `react`) to follow a single service. |
| `make logs-db` | Follows the PostgreSQL logs from the Supabase database container. |
| `make backend-compile` | Runs `./mvnw compile` inside the backend container. Spring Boot devtools detects the new classes and restarts the context in under a second. |
| `make backend-clean-compile` | Runs `./mvnw clean compile`. Use it after deleting or renaming a Java class: a plain compile leaves the old `.class` file in `target/classes`. |
| `make backend-shell` | Opens a bash shell inside the running backend container. |
| `make db-shell` | Opens `psql` against the local Supabase database (`postgres` user, `postgres` database). |

### Production

| Command | What it does |
| --- | --- |
| `make prod` | Starts the production stack (nginx on 8081, Spring Boot on 8082). Requires the production images; it fails with a clear message if they are missing. |
| `make build-prod` | Builds the production images and starts the stack. |
| `make logs-prod` | Follows the production logs. |

### Supabase

| Command | What it does |
| --- | --- |
| `make supabase-start` | Starts the local Supabase stack if it is not already running. `make dev`, `build-dev`, `prod` and `build-prod` call this automatically. |
| `make supabase-status` | Prints the Supabase URLs and keys (API URL, database URL, anon key, service role key, Studio, Mailpit). |
| `make supabase-reset` | Stops Supabase with `--no-backup` (deletes all local data) and starts it again. Asks for confirmation; use `CONFIRM=1 make supabase-reset` in scripts. |

### Status and cleanup

| Command | What it does |
| --- | --- |
| `make status` | Shows the containers of `incidea-dev`, `incidea-prod` and the Supabase stack, plus all local URLs. |
| `make stop` | Stops the app (dev **and** prod) and Supabase. Data volumes are preserved. |
| `make down` | Removes the app containers and networks (dev and prod). Supabase keeps running. |
| `make clean` | Asks for confirmation, then removes the app containers, volumes and the locally built images. Supabase data is **not** touched. Use `CONFIRM=1 make clean` in scripts. |

### What each cleanup command affects

| Command | App containers | App networks | App images | App volumes | Supabase data |
| --- | --- | --- | --- | --- | --- |
| `make stop` | Stopped | Kept | Kept | Kept | Kept |
| `make down` | Removed | Removed | Kept | Kept | Kept |
| `make clean` | Removed | Removed | Removed | Removed | Kept |
| `make supabase-reset` | Untouched | Untouched | Untouched | Untouched | **Deleted** |

## Services and ports

| Service | Host | Container |
| --- | --- | --- |
| React dev (Vite) | 3000 | 3000 |
| Backend dev (Spring Boot) | 8080 | 8080 |
| React prod (nginx) | 8081 | 80 |
| Backend prod (Spring Boot) | 8082 | 8080 |
| Supabase API (Kong) | 54321 | 8000 |
| Supabase PostgreSQL | 54322 | 5432 |
| Supabase Studio | 54323 | 3000 |
| Supabase Mailpit | 54324 | 8025 |
| Supabase Analytics | 54327 | 4000 |

## Supabase containers

| Container | Role |
| --- | --- |
| `supabase_db_incidea-app` | PostgreSQL 17 |
| `supabase_kong_incidea-app` | API gateway (REST, Auth, Storage, Realtime) |
| `supabase_auth_incidea-app` | GoTrue: users, sessions, OAuth providers |
| `supabase_rest_incidea-app` | PostgREST |
| `supabase_storage_incidea-app` | Storage API |
| `supabase_realtime_incidea-app` | Realtime subscriptions |
| `supabase_studio_incidea-app` | Studio UI |
| `supabase_inbucket_incidea-app` | Mailpit (captures outgoing emails) |
| `supabase_pg_meta_incidea-app` | postgres-meta, used by Studio |
| `supabase_edge_runtime_incidea-app` | Edge Functions runtime |
| `supabase_analytics_incidea-app` | Logflare analytics |
| `supabase_vector_incidea-app` | Log collector |

## Networks and volumes

| Name | Type | Purpose |
| --- | --- | --- |
| `incidea-dev_default` | Network | Communication between `react` and `backend` in development |
| `incidea-prod_default` | Network | Communication between `react-prod` and `backend-prod` |
| `supabase_network_incidea-app` | External network | Created by the Supabase CLI; the backend joins it to reach `db:5432` |
| `incidea-dev_backend-m2` | Volume | Maven cache (`/home/ubuntu/.m2`) for the dev backend |
| Anonymous `/app/node_modules` volume | Volume | Keeps the image's `node_modules` visible under the frontend bind mount |
| `supabase_db_incidea-app` | Volume | Local database files |
| `supabase_storage_incidea-app` | Volume | Local storage files |
| `supabase_edge_runtime_incidea-app` | Volume | Edge Functions data |

The production build also uses a BuildKit cache (`id=backend-m2`) so Maven
dependencies are not downloaded again on every rebuild.

## How the Docker images work

Each application has two Dockerfiles: one for development and one for
production. They optimize for different things.

### Frontend development (`incideafrontend/Dockerfile.dev`)

- Base `node:24.22.1-alpine` with Corepack activating pnpm 12.6.0.
- Installs dependencies with `pnpm fetch` + `pnpm install --frozen-lockfile`
  using a pnpm store cache mount.
- Runs `pnpm dev --host 0.0.0.0`.
- Compose bind-mounts `incideafrontend/` over `/app` and keeps the container's
  `node_modules` in an anonymous volume. Editing a file on the host triggers
  Vite HMR; the image is never rebuilt for code changes.

### Frontend production (`incideafrontend/Dockerfile`)

- **Build stage:** Node + pnpm, `pnpm install --frozen-lockfile`, then
  `pnpm exec tsc --noEmit && pnpm build`. The explicit `tsc` step is the type
  gate: Vite/esbuild strips types without checking them.
- **Runtime stage:** `nginx:1.29-alpine` serving `dist/`.
- `nginx.conf` mirrors a static SPA deployment (Vercel style):
  - hashed `/assets/` are cached with `immutable`;
  - `index.html` is served with `no-store`;
  - deep links fall back to `index.html`;
  - gzip is enabled;
  - `/api/` is proxied to `http://backend:8080/api/`, so the browser sees a
    single origin and there is no CORS.
- Includes a `wget` healthcheck.

### Backend development (`incideabackend/Dockerfile.dev`)

- Base `eclipse-temurin:21-jdk` plus `unzip` (needed by the Maven wrapper).
- Uses uid 1000 (the image's `ubuntu` user) so files written to the bind mount
  (`target/`) stay owned by your host user instead of root.
- Runs `./mvnw dependency:go-offline` at build time: dependencies are baked
  into the image.
- Starts with `./mvnw spring-boot:run`.
- Compose bind-mounts `incideabackend/` over `/app` and keeps the Maven cache in
  the `backend-m2` volume.
- Java has no true hot reload. The workflow is: edit Java -> run
  `make backend-compile` -> devtools restarts the application context.

### Backend production (`incideabackend/Dockerfile`)

- **Build stage:** `eclipse-temurin:21-jdk`, `dependency:go-offline`, then
  `./mvnw package` with a BuildKit Maven cache mount.
- Tests are compiled but not executed (`-DskipTests`): they need a real
  database or Testcontainers.
- **Runtime stage:** `eclipse-temurin:21-jre-alpine`, non-root user `app`
  (uid 10001), only the fat JAR.
- `JAVA_TOOL_OPTIONS` caps heap usage at 75% of the container memory and exits
  on `OutOfMemoryError`.
- Healthcheck against `/actuator/health/liveness`.

### Why two Dockerfiles per app

| | Development | Production |
| --- | --- | --- |
| Source | Bind-mounted from the host | Baked into the image |
| Frontend | Vite dev server with HMR | Static bundle served by nginx |
| Backend | `spring-boot:run` + devtools | Executable fat JAR on a JRE |
| Rebuild on code change | No | Yes, by design |
| Goal | Fast feedback | Small, immutable artifact |

## Environment variables

`incideainfraestructure/.env` is ignored by Git. `make setup` creates it from
`.env.example`:

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SUPABASE_NETWORK=supabase_network_incidea-app
```

| Variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL. Uses the Compose service name `db`, not `localhost`. |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Local Supabase database credentials. |
| `SUPABASE_NETWORK` | Name of the external Docker network created by the Supabase CLI. |

Application configuration lives in
`incideabackend/src/main/resources/application.properties`:

- Actuator exposes only `health` (used by the healthchecks).
- Flyway and Hibernate use the `app` schema, keeping the backend away from
  Supabase's internal schemas (`auth`, `storage`, `realtime`, ...).
- Hibernate's `open-in-view` is disabled.

**Secrets:** the `anon` key is public by design; the `service_role` key and
OAuth client secrets must never be committed. Keep them in `.env` or in your
secret manager.

## Daily workflow

```bash
# Start the day
make dev

# Frontend: edit a file, Vite updates the browser (HMR), no rebuild
# Backend: edit a Java file, then compile and let devtools restart
make backend-compile

# Follow logs
make logs
make logs SERVICE=backend
make backend-shell

# Database
make db-shell
# Studio: http://localhost:54323

# End of the day (app + Supabase, data kept)
make stop
```

Adding dependencies:

```bash
# Frontend
cd incideafrontend
pnpm add <package>
cd ..
make build-dev

# Backend: edit incideabackend/pom.xml, then
make build-dev
```

After deleting or renaming a Java class:

```bash
make backend-clean-compile
```

## Production

```bash
make build-prod
```

- Frontend: <http://localhost:8081>
- Backend: <http://localhost:8082>

The production stack uses the same Supabase instance as development. Both
compose projects are isolated at the container level, but they share the local
database. If you need isolated data (for example, to test OAuth flows without
mixing users), point one of the stacks to a different Supabase project.

## Running without Docker

The Docker setup is the recommended path, but you can run the apps natively.

```bash
# 1. Database and Supabase services
make supabase-start

# 2. Frontend
cd incideafrontend
pnpm install
pnpm dev

# 3. Backend (Java 21 + Maven wrapper)
cd incideabackend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:54322/postgres \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./mvnw spring-boot:run
```

Native mode requires Node.js 24 (`^20.19.0` or `>=22.12.0` also work for Vite 8)
and JDK 21. The Docker images already include the correct versions.

## Troubleshooting

| Problem | Solution |
| --- | --- |
| `network supabase_network_incidea-app not found` | Supabase is not running. Run `make supabase-start` or simply `make dev`. |
| `Development images not found` | Run `make build-dev` (first run or after `make clean`). |
| `Production images not found` | Run `make build-prod`. |
| `Docker is not running` | Start Docker Desktop and retry. |
| Port already in use | Run `ss -ltnp` and look for the conflicting port. |
| A deleted Java class still responds | `make backend-clean-compile`; a plain compile leaves stale `.class` files. |
| Supabase containers restart when Docker starts | They use `restart: unless-stopped`. Run `make stop` to stop them; `make dev` starts them again. |
| `Supabase CLI not found` | Run `make setup` (installs it with pnpm in `incideainfraestructure`). |
| Want a clean slate | `make clean` (app only) and/or `make supabase-reset` (deletes local database data). |
| Supabase ports are reachable from the network | This CLI version publishes them on `0.0.0.0`. On shared machines, restrict access with a firewall. |

## Architecture notes

- **`make dev` / `make prod` recreate the containers:** the Supabase CLI removes
  its Docker network when the stack stops and creates a new one on start.
  Reusing stopped containers would keep a reference to the old network and fail
  with `network ... not found`, so the Makefile uses `--force-recreate`.
- **Backend schema:** Flyway creates and migrates the `app` schema. Supabase's
  internal schemas are left untouched.
- **Supabase CLI:** installed as a dev dependency in `incideainfraestructure`.
  `node_modules` is ignored by Git; `package.json` and `pnpm-lock.yaml` are
  committed so the CLI version is reproducible.
- **Image sizes:** frontend dev ~660 MB, frontend prod ~93 MB, backend dev
  ~1 GB (JDK + Maven cache), backend prod ~400 MB (JRE + fat JAR).
- **Tests:** not executed while building images. Run them with a database
  available (for example, after `make supabase-start`).
