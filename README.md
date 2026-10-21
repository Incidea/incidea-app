# Incidea App

The repository is organized into two independent projects: a React frontend and a Spring Boot backend.

## Frontend - React

The frontend lives in [`incideafrontend`](./incideafrontend) and is built with **React 19**, **Vite 8**, **pnpm 12** and **TanStack Router** (file-based routing) with **Tailwind CSS 4**.

### Requirements

| Requirement | Details |
| --- | --- |
| **Node.js** | `^20.19.0` or `>=22.12.0` (required by Vite 8 and TanStack Router). The project was installed and tested with Node.js 24.21.0. |
| **pnpm 12.6.0** | Pinned through the `packageManager` field. Node's Corepack can use it automatically, or you can install it with `npm install -g pnpm@12.6.0`. |
| **Free port 3000** | Vite's development server is configured to use port 3000. |

## Backend - Spring Boot

The backend is located in the [`incideabackend`](./incideabackend) directory and is built with **Spring Boot 4.1.1** and **Java 21**.

### Requirements to run the application

| Requirement | Details |
| --- | --- |
| **JDK 21** | The `pom.xml` sets `<java.version>21</java.version>`. Spring Boot 4.1.1 requires Java 17 or higher, but the project is compiled and intended for Java 21. |
| **Maven** | No need to install it: the project includes the Maven Wrapper (`mvnw`/`mvnw.cmd`), which automatically downloads and uses Maven 3.10.0. |
| **PostgreSQL** | The application uses Spring Data JPA and the PostgreSQL driver, so it needs an accessible PostgreSQL database. |
| **Free port 8080** | This is Spring Boot's default HTTP port (Tomcat). |
| **Docker (optional)** | Only if you want to run PostgreSQL in a container or, later, use Testcontainers in the tests. |

Check your Java version before starting:

```bash
java -version
# It must show a 21.x version
```