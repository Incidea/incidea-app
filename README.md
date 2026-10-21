# incidea-app

The repository is organized into two independent projects: a React frontend and a Spring Boot backend.

## Frontend - React

The frontend lives in [`incideafrontend`](./incideafrontend) and is built with **React 19**, **Vite 8**, **pnpm 12** and **TanStack Router** (file-based routing) with **Tailwind CSS 4**.

### Requirements

| Requirement | Details |
| --- | --- |
| **Node.js** | `^20.19.0` or `>=22.12.0` (required by Vite 8 and TanStack Router). The project was installed and tested with Node.js 24.21.0. |
| **pnpm 12.6.0** | Pinned through the `packageManager` field. Node's Corepack can use it automatically, or you can install it with `npm install -g pnpm@12.6.0`. |
| **Free port 3000** | Vite's development server is configured to use port 3000. |