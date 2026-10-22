.PHONY: help dev prod up-dev up-prod down-dev down-prod build-dev build-prod stop clean logs-dev logs-prod backend-compile logs-backend supabase-start supabase-stop supabase-status

# Supabase CLI instalado con pnpm en incideainfraestructure
SUPABASE ?= ./incideainfraestructure/node_modules/.bin/supabase

# Colores para output
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m # No Color


help:
	@echo "$(GREEN)Available commands:$(NC)"
	@echo ""
	@echo "$(YELLOW)Development (with hot-reload):$(NC)"
	@echo "  make dev         - Start development environment (hot-reload enabled)"
	@echo "  make up-dev      - Start development containers"
	@echo "  make build-dev   - Build and start development environment"
	@echo "  make logs-dev    - Show development container logs"
	@echo "  make logs-backend - Show backend logs"
	@echo "  make backend-compile - Compile Java changes (devtools restarts the app)"
	@echo ""
	@echo "$(YELLOW)Production (standalone):$(NC)"
	@echo "  make prod        - Start production environment (optimized)"
	@echo "  make up-prod     - Start production containers"
	@echo "  make build-prod  - Build and start production environment"
	@echo "  make logs-prod   - Show production container logs"
	@echo ""
	@echo "$(YELLOW)Supabase (local DB/API):$(NC)"
	@echo "  make supabase-start  - Start the local Supabase stack (needed by backend)"
	@echo "  make supabase-status - Show Supabase URLs and keys"
	@echo "  make supabase-stop   - Stop the Supabase stack"
	@echo ""
	@echo "$(YELLOW)Common commands:$(NC)"
	@echo "  make stop        - Stop all containers"
	@echo "  make down        - Stop and remove all containers"
	@echo "  make clean       - Remove all containers, volumes and images"
	@echo "  make help        - Show this help message"

# 
# Development commands (hot-reload)
# 
dev: supabase-start
	@echo "$(GREEN)Starting development environment with hot-reload...$(NC)"
	cd incideainfraestructure && docker-compose -f compose.dev.yaml up -d
	@sleep 5
	@echo "$(GREEN)✅ Development environment ready!$(NC)"
	@echo "📌 React.js (dev): http://localhost:3000"
	@echo "📌 Spring Boot (dev): http://localhost:8080"
	@echo "⚡ Hot-reload enabled - changes will auto-refresh"

up-dev: supabase-start
	cd incideainfraestructure && docker-compose -f compose.dev.yaml up -d

build-dev: supabase-start
	cd incideainfraestructure && docker-compose -f compose.dev.yaml up -d --build
	@sleep 5
	@echo "$(GREEN)✅ Development environment built and started!$(NC)"
	@echo "📌 React.js (dev): http://localhost:3000"

logs-dev:
	cd incideainfraestructure && docker-compose -f compose.dev.yaml logs -f

logs-backend:
	cd incideainfraestructure && docker-compose -f compose.dev.yaml logs -f backend

backend-compile:
	@echo "$(GREEN)Compiling backend (devtools will restart the app)...$(NC)"
	cd incideainfraestructure && docker-compose -f compose.dev.yaml exec backend ./mvnw -q compile

down-dev:
	cd incideainfraestructure && docker-compose -f compose.dev.yaml down

# 
# Production commands (standalone)
# 
prod: supabase-start
	@echo "$(GREEN)Starting production environment...$(NC)"
	cd incideainfraestructure && docker-compose -f compose.yaml up -d
	@sleep 5
	@echo "$(GREEN)✅ Production environment ready!$(NC)"
	@echo "📌 React.js (prod): http://localhost:8081"
	@echo "📌 Spring Boot (prod): http://localhost:8082"

up-prod: supabase-start
	cd incideainfraestructure && docker-compose -f compose.yaml up -d

build-prod: supabase-start
	cd incideainfraestructure && docker-compose -f compose.yaml up -d --build
	@sleep 5
	@echo "$(GREEN)✅ Production environment built and started!$(NC)"
	@echo "📌 React.js (prod): http://localhost:8081"
	@echo "📌 Spring Boot (prod): http://localhost:8082"

logs-prod:
	cd incideainfraestructure && docker-compose -f compose.yaml logs -f

down-prod:
	cd incideainfraestructure && docker-compose -f compose.yaml down

#
# Supabase (local DB/API used by the backend)
#
supabase-start:
	@test -x $(SUPABASE) || (echo "$(YELLOW)Supabase CLI no instalado. Ejecuta: cd incideainfraestructure && pnpm install$(NC)"; exit 1)
	$(SUPABASE) --workdir . start

supabase-status:
	@test -x $(SUPABASE) || (echo "$(YELLOW)Supabase CLI no instalado. Ejecuta: cd incideainfraestructure && pnpm install$(NC)"; exit 1)
	$(SUPABASE) --workdir . status

supabase-stop:
	$(SUPABASE) --workdir . stop

#
# Common commands
#
stop:
	@echo "Stopping all containers..."
	cd incideainfraestructure && docker-compose -f compose.yaml stop 2>/dev/null || true
	cd incideainfraestructure && docker-compose -f compose.dev.yaml stop 2>/dev/null || true
	@echo "✅ All containers stopped"

down:
	@echo "Removing all containers..."
	cd incideainfraestructure && docker-compose -f compose.yaml down 2>/dev/null || true
	cd incideainfraestructure && docker-compose -f compose.dev.yaml down 2>/dev/null || true
	@echo "✅ All containers removed"

clean:
	@echo "Cleaning all containers, volumes and images..."
	cd incideainfraestructure && docker-compose -f compose.yaml down -v --rmi all 2>/dev/null || true
	cd incideainfraestructure && docker-compose -f compose.dev.yaml down -v --rmi all 2>/dev/null || true
	@echo "✅ Cleanup complete"