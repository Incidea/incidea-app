# **INCIDEA**

**Incidea** · Documento de decisiones técnicas · v0.1 · Octubre 2026 · Autores: Aleksander Trujillo,

[1\. Resumen del proyecto](#resumen-del-proyecto)

[2\. Tecnologías](#tecnologías)

[a. Frontend](#frontend)

[b. Backend](#backend)

[c. Base de datos](#base-de-datos)

[i. Supabase](#supabase)

[ii. Autenticación](#autenticación)

[iii. Generación de PDFs](#generación-de-pdfs)

[d. DevOps](#devops)

[i. Contenedores Docker](#contenedores-docker)

[ii. Integración y entrega continua (CI/CD)](<#integración-y-entrega-continua-(ci/cd)>)

[iii. Entornos y configuración](#entornos-y-configuración)

[iv. Despliegue](#despliegue)

[e. Testing](#testing)

[3\. Diagrama de arquitectura](#diagrama-de-arquitectura)

1. # Resumen del proyecto {#resumen-del-proyecto}

Vamos a construir un SaaS (software como servicio) para empresas que gestionan averías e incidencias de mantenimiento de terceros. El producto será un panel web (dashboard) donde se crean incidencias, se asignan a equipos técnicos, se sigue su estado y, sobre todo, se genera automáticamente el parte de trabajo en PDF a partir de un formulario con fotos y descripción. Quedan fuera del MVP: pagos, app móvil e integraciones externas. Una vez estable, se incorporará IA para asistir en la redacción de los partes.

2. # Tecnologías {#tecnologías}
   1. # Frontend {#frontend}

   La librería escogida es **React**, por su versatilidad y su gran demanda en el mercado. Es una buena opción para **herramientas internas y paneles de administración**, y con ella evitamos la complejidad del renderizado en servidor (SSR).

Podríamos haber usado **Next.js**, pero al tener un backend independiente no aprovecharemos sus server actions ni sus API routes. **React \+ Vite**, como **SPA**, encaja con un dashboard al que se accede tras iniciar sesión, y nos permite practicar hooks y gestión de estado en cliente.

Para el estado del servidor usaremos **TanStack Query**, que **gestiona la caché y la sincronización de datos con la API**. Además, el testing del frontend es una prioridad del proyecto

2. # Backend {#backend}

   La lógica de negocio se implementa en **Java** con **Spring Boot**. Podríamos haber conectado **React** directamente a **Supabase** con **Row Level Security**, que sería más rápido, pero elegimos un backend propio por estas razones:

- Las **reglas de negocio y la autorización viven en un único sitio**, lo que permite testearlas y depurarlas.
- Las claves de los LLM, el envío de emails, los webhooks o los pagos son datos sensibles que no deben estar en el frontend.
- Es más **fácil de testear** con herramientas como JUnit y Testcontainers que las políticas RLS.
- El dominio es nuestro: **no queda atado a un proveedor**.
- Es el framework de backend con el que tenemos más experiencia y con más demanda laboral.

**Stack del backend**: Spring Boot, Spring Security (como resource server que valida el JWT), Spring Data JPA y Flyway para las migraciones de base de datos. \[Indicad la versión de Java.\]

Contrapartida: son dos proyectos que mantener y más superficie de despliegue. Lo asumimos a cambio de control y de reglas de negocio centralizadas.

3. # Base de datos {#base-de-datos}

   Necesitamos una **base de datos relacional**, porque los datos del producto (usuarios, equipos, incidencias, asignaciones) están muy conectados entre sí. Entre las opciones relacionales elegimos **PostgreSQL frente a MySQL** por estos motivos:

- **JSONB**: permite **guardar datos semiestructurados** (metadatos de una incidencia, campos personalizados por equipo) con índices y consultas potentes, sin salir del modelo relacional.
- **Búsqueda de texto integrada**: el full-text search nativo nos permite buscar incidencias por título o descripción sin añadir un motor externo.
- **Extensibilidad**: extensiones como pgvector abren la puerta a búsqueda semántica de incidencias similares cuando **incorporamos IA**, sin añadir otra base de datos.
- **Migraciones transaccionales**: PostgreSQL **admite DDL** dentro de transacciones, así que una migración de Flyway que falle se revierte entera. En MySQL, los cambios de esquema provocan commits implícitos.
- **Tipos y funciones avanzadas**: UUID nativo, arrays, enums, CTEs recursivas y funciones de ventana, útiles para historiales e informes.
- **Ecosistema**: es el estándar en los SaaS actuales y es la base de Supabase.

**Contrapartida**: MySQL también habría sido una opción válida y es muy rápido en lecturas simples; para el volumen de un MVP el rendimiento no es un factor diferencial. La elección pesa más por las capacidades de PostgreSQL y por su integración con Supabase.

1. ### Supabase {#supabase}

Para alojar **PostgreSQL** usamos **Supabase**, que nos ofrece la base de datos gestionada junto con autenticación y almacenamiento. Es PostgreSQL estándar, así que no quedamos atados a una tecnología propietaria. Además, **no tenemos que administrar servidores de base de datos** y el plan gratuito es suficiente para desarrollar y validar el MVP.

**Limitación conocida**: en el plan gratuito, los proyectos se paran tras una semana sin actividad y hay que activarlos manualmente. Para una versión en producción habría que pasar a un plan de pago.

2. ### Autenticación {#autenticación}

La autenticación la resuelve **Supabase Auth**, con email/contraseña y Google, además de verificación de email y recuperación de contraseña. El flujo es:

- El **frontend inicia sesión directamente contra Supabase Auth** con supabase-js.
- Supabase **devuelve un JWT**.
- El frontend lo envía al backend en la cabecera Authorization: Bearer.
- Spring Security lo valida y aplica la autorización (roles, pertenencia a equipos) con datos de nuestras propias tablas.

**Alternativa descartada**: implementar la autenticación a mano en Spring. Supone más trabajo y más riesgo de seguridad, y no aporta valor diferencial al producto. Lo que sí implementamos nosotros es la autorización.

3. ### Generación de PDFs {#generación-de-pdfs}

El parte de trabajo se genera en el backend a partir del formulario (descripción y fotos). Las fotos y los PDF generados se almacenan en Supabase Storage. \[Pendiente de decidir: librería de generación de PDF, por ejemplo OpenPDF, Apache PDFBox o plantilla HTML convertida a PDF.\]

4. # DevOps {#devops}
   1. ### Contenedores Docker {#contenedores-docker}

   Qué empaquetamos: el **backend va en una imagen Docker**. El frontend no necesita contenedor en producción, porque se despliega como SPA estática en Vercel.

- **Multi-stage build**: una imagen compila el proyecto con Maven o Gradle y otra, más ligera, ejecuta solo el JAR con el JRE.
- **Entorno local**: el backend se levanta con _docker compose up_ y los servicios de Supabase (PostgreSQL, Auth y Storage) con la CLI de Supabase (supabase start). 2. ### Integración y entrega continua (CI/CD) {#integración-y-entrega-continua-(ci/cd)}

Para la integración continua usamos GitHub Actions, integrado en el repositorio donde ya alojamos el código, sin herramientas adicionales. Es gratuito para repositorios públicos y cuenta con abundante documentación y acciones reutilizables.

- En cada **pull request**: lint, tests de backend y frontend y build del backend.
- **Protección de main**: no se puede fusionar con checks en rojo.
- Al hacer merge a **main**: Vercel y Render despliegan automáticamente frontend y backend. 3. ### Entornos y configuración {#entornos-y-configuración}

- **Local**: docker compose para el backend y Supabase CLI para base de datos, Auth y Storage.
- **Producción**: Vercel (frontend), Render (backend) y Supabase (base de datos, Auth y Storage).
- **Staging**: opcional, se añadirá cuando haya usuarios reales. 4. ### Despliegue {#despliegue}

- **Vercel** aloja el frontend **(SPA de Vite)**. Requiere una regla de reescritura a index.html para que las rutas de la SPA funcionen al recargar.
- **Render** ejecuta el **backend desde su Dockerfile**. Hay que configurar CORS para aceptar el dominio de Vercel.
- Las variables de entorno se configuran en cada plataforma (VITE\_... en Vercel, las de Spring en Render) 5. # Testing {#testing}

Realizaremos pruebas automáticas que protegen las reglas de negocio y los flujos críticos.

**Backend**:

1. **JUnit 5 \+ Mockito**: unitarios de la lógica de negocio.
2. **Testcontainers con PostgreSQL**: integración con una base de datos real.
3. **Spring Security Test**: roles y permisos (quién puede ver y modificar qué).

**Frontend**:

1. **Vitest \+ React Testing Library**: componentes y hooks.
2. **MSW**: simula la API para testear sin backend.
3. **Playwright**: 1-2 flujos E2E (login y crear incidencia).

**Criterio**: priorizamos autorización, reglas de negocio y flujos principales por encima del porcentaje de cobertura. Los tests se ejecutan automáticamente en cada PR (conecta con la sección CI/CD). Cada pull request ejecuta los tests de la sección CI/CD, y una regla de protección impide fusionar si fallan.
