# syntax=docker/dockerfile:1.7
#
# Production image: builds the Vite bundle and serves it with nginx,
# mirroring how Vercel serves a static SPA (SPA fallback, immutable hashed
# assets, no-store index.html, gzip).
#
ARG NODE_VERSION=24.22.1-alpine

FROM node:${NODE_VERSION} AS build

ENV PNPM_HOME=/pnpm \
    PATH=/pnpm:$PATH \
    COREPACK_HOME=/opt/corepack

RUN corepack enable \
 && corepack prepare pnpm@12.6.0 --activate \
 && pnpm config set store-dir /pnpm/store

WORKDIR /app

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN --mount=type=cache,id=pnpm-store,target=/pnpm/store \
    pnpm install --frozen-lockfile

COPY . .

# Real build gate: vite strips types with esbuild, so tsc must run separately.
RUN pnpm exec tsc --noEmit && pnpm build

FROM nginx:1.29-alpine AS prod

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
