SHELL := /bin/bash
.DEFAULT_GOAL := help

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
COMPOSE ?= docker compose
INFRA   := incideainfraestructure

COMPOSE_DEV  := $(COMPOSE) -f compose.dev.yaml
COMPOSE_PROD := $(COMPOSE) -f compose.yaml

SUPABASE         := $(INFRA)/node_modules/.bin/supabase
SUPABASE_PROJECT := $(or $(shell sed -n 's/^project_id = "\(.*\)"/\1/p' supabase/config.toml 2>/dev/null),incidea-app)
SUPABASE_DB      := supabase_db_$(SUPABASE_PROJECT)

DEV_FRONTEND    := http://localhost:3000
DEV_BACKEND     := http://localhost:8080
PROD_FRONTEND   := http://localhost:8081
PROD_BACKEND    := http://localhost:8082
SUPABASE_STUDIO := http://localhost:54323

GREEN  := \033[0;32m
YELLOW := \033[1;33m
CYAN   := \033[0;36m
RESET  := \033[0m

# ---------------------------------------------------------------------------
# Help
# ---------------------------------------------------------------------------
help:
	@printf "$(CYAN)Incidea App$(RESET)\n\n"
	@printf "$(YELLOW)Setup$(RESET)\n"
	@printf "  %-28s %s\n" "make setup" "Install the Supabase CLI and create .env"
	@printf "\n$(YELLOW)Development$(RESET)\n"
	@printf "  %-28s %s\n" "make dev" "Start Supabase + development stack"
	@printf "  %-28s %s\n" "make build-dev" "Rebuild images and start development stack"
	@printf "  %-28s %s\n" "make logs" "Follow development logs (SERVICE=backend for one)"
	@printf "  %-28s %s\n" "make logs-db" "Follow the Supabase PostgreSQL logs"
	@printf "  %-28s %s\n" "make backend-compile" "Compile Java; devtools restarts the app"
	@printf "  %-28s %s\n" "make backend-clean-compile" "Clean compile (use after deleting classes)"
	@printf "  %-28s %s\n" "make backend-shell" "Open a shell in the backend container"
	@printf "  %-28s %s\n" "make db-shell" "Open psql against the local database"
	@printf "\n$(YELLOW)Production$(RESET)\n"
	@printf "  %-28s %s\n" "make prod" "Start the production stack"
	@printf "  %-28s %s\n" "make build-prod" "Rebuild images and start the production stack"
	@printf "  %-28s %s\n" "make logs-prod" "Follow production logs"
	@printf "\n$(YELLOW)Supabase$(RESET)\n"
	@printf "  %-28s %s\n" "make supabase-start" "Start the local Supabase stack"
	@printf "  %-28s %s\n" "make supabase-status" "Show Supabase URLs and keys"
	@printf "  %-28s %s\n" "make supabase-reset" "Delete local Supabase data and restart"
	@printf "\n$(YELLOW)Status and cleanup$(RESET)\n"
	@printf "  %-28s %s\n" "make status" "Show containers and URLs"
	@printf "  %-28s %s\n" "make stop" "Stop the app and Supabase (data kept)"
	@printf "  %-28s %s\n" "make down" "Remove app containers and networks"
	@printf "  %-28s %s\n" "make clean" "Remove app images and volumes (data kept)"
	@printf "\n$(YELLOW)Help$(RESET)\n"
	@printf "  %-28s %s\n" "make help" "Show this help"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
ensure-env:
	@test -f $(INFRA)/.env || cp $(INFRA)/.env.example $(INFRA)/.env
	@test -x $(SUPABASE) || { printf "Supabase CLI not found. Run: make setup\n"; exit 1; }

check-docker:
	@docker info >/dev/null 2>&1 || { printf "Docker is not running.\n"; exit 1; }

check-dev-images:
	@docker image inspect incidea-frontend:dev incidea-backend:dev >/dev/null 2>&1 || { \
		printf "Development images not found. Run: make build-dev\n"; exit 1; }

check-prod-images:
	@docker image inspect incidea-frontend:prod incidea-backend:prod >/dev/null 2>&1 || { \
		printf "Production images not found. Run: make build-prod\n"; exit 1; }

# ---------------------------------------------------------------------------
# Setup
# ---------------------------------------------------------------------------
setup: check-docker
	@command -v pnpm >/dev/null 2>&1 || { printf "pnpm is required: https://pnpm.io/installation\n"; exit 1; }
	@test -f $(INFRA)/.env || cp $(INFRA)/.env.example $(INFRA)/.env
	cd $(INFRA) && pnpm install --frozen-lockfile
	@printf "$(GREEN)Setup complete.$(RESET) Next: make build-dev\n"

# ---------------------------------------------------------------------------
# Development
# ---------------------------------------------------------------------------
# --force-recreate: `supabase stop` removes the Supabase network, and the CLI
# creates a new one on start. Reusing stopped containers would keep a stale
# network reference and fail with "network ... not found".
dev: ensure-env check-docker check-dev-images supabase-start
	@printf "$(GREEN)Starting development stack...$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_DEV) up -d --force-recreate --wait --wait-timeout 180 --remove-orphans
	@printf "$(GREEN)Ready.$(RESET)\n"
	@printf "  %-12s %s\n" "Frontend" "$(DEV_FRONTEND)"
	@printf "  %-12s %s\n" "Backend" "$(DEV_BACKEND)"
	@printf "  %-12s %s\n" "Studio" "$(SUPABASE_STUDIO)"

build-dev: ensure-env check-docker supabase-start
	@printf "$(GREEN)Building and starting development stack...$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_DEV) up -d --build --force-recreate --wait --wait-timeout 300 --remove-orphans
	@printf "$(GREEN)Ready.$(RESET)\n"
	@printf "  %-12s %s\n" "Frontend" "$(DEV_FRONTEND)"
	@printf "  %-12s %s\n" "Backend" "$(DEV_BACKEND)"
	@printf "  %-12s %s\n" "Studio" "$(SUPABASE_STUDIO)"

logs:
	@cd $(INFRA) && $(COMPOSE_DEV) logs -f $(SERVICE)

logs-db:
	@docker logs -f $(SUPABASE_DB)

backend-compile:
	@printf "$(GREEN)Compiling backend (devtools will restart the app)...$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_DEV) exec -T backend ./mvnw -q compile

backend-clean-compile:
	@printf "$(GREEN)Clean compiling backend (devtools will restart the app)...$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_DEV) exec -T backend ./mvnw -q clean compile

backend-shell:
	@cd $(INFRA) && $(COMPOSE_DEV) exec backend bash

db-shell:
	@docker exec -it $(SUPABASE_DB) psql -U postgres -d postgres

# ---------------------------------------------------------------------------
# Production
# ---------------------------------------------------------------------------
prod: ensure-env check-docker check-prod-images supabase-start
	@printf "$(GREEN)Starting production stack...$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_PROD) up -d --force-recreate --wait --wait-timeout 180 --remove-orphans
	@printf "$(GREEN)Ready.$(RESET)\n"
	@printf "  %-12s %s\n" "Frontend" "$(PROD_FRONTEND)"
	@printf "  %-12s %s\n" "Backend" "$(PROD_BACKEND)"

build-prod: ensure-env check-docker supabase-start
	@printf "$(GREEN)Building and starting production stack...$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_PROD) up -d --build --force-recreate --wait --wait-timeout 300 --remove-orphans
	@printf "$(GREEN)Ready.$(RESET)\n"
	@printf "  %-12s %s\n" "Frontend" "$(PROD_FRONTEND)"
	@printf "  %-12s %s\n" "Backend" "$(PROD_BACKEND)"

logs-prod:
	@cd $(INFRA) && $(COMPOSE_PROD) logs -f

# ---------------------------------------------------------------------------
# Supabase
# ---------------------------------------------------------------------------
supabase-start: ensure-env check-docker
	@if docker ps -q --filter label=com.supabase.cli.project=$(SUPABASE_PROJECT) | grep -q .; then \
		printf "Supabase is already running.\n"; \
	else \
		printf "$(GREEN)Starting Supabase...$(RESET)\n"; \
		$(SUPABASE) --workdir . start; \
	fi

supabase-status: ensure-env
	@$(SUPABASE) --workdir . status

supabase-reset: ensure-env check-docker
	@if [ "$$CONFIRM" != "1" ]; then \
		printf "Delete ALL local Supabase data (users, tables, storage)? [y/N] "; \
		read -r ans; \
		case "$$ans" in y|Y) ;; *) printf "Cancelled.\n"; exit 1;; esac; \
	fi
	@$(SUPABASE) --workdir . stop --no-backup 2>/dev/null || true
	@$(MAKE) --no-print-directory supabase-start

# ---------------------------------------------------------------------------
# Status and cleanup
# ---------------------------------------------------------------------------
status:
	@docker info >/dev/null 2>&1 || { printf "Docker is not running.\n"; exit 0; }
	@printf "$(CYAN)incidea-dev$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_DEV) ps -a
	@printf "\n$(CYAN)incidea-prod$(RESET)\n"
	@cd $(INFRA) && $(COMPOSE_PROD) ps -a
	@printf "\n$(CYAN)Supabase$(RESET)\n"
	@docker ps -a --filter label=com.supabase.cli.project=$(SUPABASE_PROJECT) --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
	@printf "\n$(CYAN)URLs$(RESET)\n"
	@printf "  %-12s %s\n" "Dev" "$(DEV_FRONTEND)"
	@printf "  %-12s %s\n" "Dev API" "$(DEV_BACKEND)"
	@printf "  %-12s %s\n" "Prod" "$(PROD_FRONTEND)"
	@printf "  %-12s %s\n" "Prod API" "$(PROD_BACKEND)"
	@printf "  %-12s %s\n" "Studio" "$(SUPABASE_STUDIO)"

stop:
	@docker info >/dev/null 2>&1 || { printf "Docker is not running.\n"; exit 0; }
	@printf "Stopping the app and Supabase (data volumes are preserved)...\n"
	@cd $(INFRA) && $(COMPOSE_PROD) stop 2>/dev/null || true
	@cd $(INFRA) && $(COMPOSE_DEV) stop 2>/dev/null || true
	@if docker ps -q --filter label=com.supabase.cli.project=$(SUPABASE_PROJECT) | grep -q .; then \
		if [ -x $(SUPABASE) ]; then \
			$(SUPABASE) --workdir . stop; \
		else \
			printf "Supabase CLI not found; stopping its containers with Docker.\n"; \
			docker stop $$(docker ps -q --filter label=com.supabase.cli.project=$(SUPABASE_PROJECT)); \
		fi; \
	else \
		printf "Supabase is not running.\n"; \
	fi
	@printf "$(GREEN)Stopped.$(RESET)\n"

down:
	@docker info >/dev/null 2>&1 || { printf "Docker is not running.\n"; exit 0; }
	@printf "Removing application containers and networks...\n"
	@cd $(INFRA) && $(COMPOSE_PROD) down --remove-orphans 2>/dev/null || true
	@cd $(INFRA) && $(COMPOSE_DEV) down --remove-orphans 2>/dev/null || true
	@printf "$(GREEN)Done.$(RESET) Supabase was left untouched.\n"

clean:
	@docker info >/dev/null 2>&1 || { printf "Docker is not running.\n"; exit 0; }
	@if [ "$$CONFIRM" != "1" ]; then \
		printf "Delete app containers, volumes and images? Supabase data is kept. [y/N] "; \
		read -r ans; \
		case "$$ans" in y|Y) ;; *) printf "Cancelled.\n"; exit 1;; esac; \
	fi
	@cd $(INFRA) && $(COMPOSE_PROD) down -v --remove-orphans 2>/dev/null || true
	@cd $(INFRA) && $(COMPOSE_DEV) down -v --remove-orphans 2>/dev/null || true
	@docker image rm incidea-frontend:dev incidea-frontend:prod incidea-backend:dev incidea-backend:prod 2>/dev/null || true
	@printf "$(GREEN)App images and volumes removed.$(RESET) Supabase untouched.\n"

.PHONY: help setup ensure-env check-docker check-dev-images check-prod-images \
	dev build-dev logs logs-db backend-compile backend-clean-compile backend-shell db-shell \
	prod build-prod logs-prod \
	supabase-start supabase-status supabase-reset \
	status stop down clean
