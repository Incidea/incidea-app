# Incidea · Hoja de ruta del MVP

SaaS para empresas de mantenimiento: gestión de incidencias, equipos técnicos y generación automática del parte de trabajo en PDF.

> Cada sprint dura 1-2 semanas (ajustar al tiempo disponible). Cada sprint es un **corte vertical**: base de datos, backend, frontend y tests de una funcionalidad completa.

## Estado actual

- [x] Supabase funcionando
- [x] Backend Spring Boot y frontend React
- [x] Todo dockerizado y orquestado con `compose.yaml`
- [x] Rama `main` protegida (PR obligatorio)
- [x] README
- [x] Healthcheck definido en `compose.yaml`
- [ ] CI en GitHub Actions
- [X] `AGENTS.md`
- [ ] Migraciones y modelo de datos
- [ ] Endpoints, autenticación y CORS

## Definición de hecho (todas las tareas)

- Tests escritos y pasando
- CI en verde
- PR revisado por la otra persona y fusionado con *squash*
- Documentación actualizada si cambia algo del funcionamiento
- Ninguna migración ya aplicada se edita: se crea una nueva

## Convenciones

- **Ramas:** `feat/`, `fix/`, `docs/`, `chore/`
- **Commits:** estilo convencional (`feat: ...`, `fix: ...`)
- **Paquetes Java:** por funcionalidad (`incident/`, `user/`, `organization/`, `team/`, `common/`)
- **Base de datos:** Flyway manda sobre el esquema, con `spring.jpa.hibernate.ddl-auto=validate`
- **Rol:** enum en Java, guardado como texto (`@Enumerated(EnumType.STRING)`) y con `CHECK` en la base de datos
- **Multiempresa:** toda consulta de negocio filtra por organización

---

## Sprint 0 · Cerrar el esqueleto (todo en local)

**Objetivo:** que el proyecto tenga una base sólida antes de escribir funcionalidades.

- [ ] Crear `AGENTS.md` (y `CLAUDE.md` apuntando a él, si se usa Claude Code):
  - [ ] Stack y versiones
  - [ ] Comandos para arrancar, testear y construir
  - [ ] Estructura de paquetes y convenciones de nombres
  - [ ] Reglas del dominio (aislamiento por organización, estados y transiciones, parte editable solo en En curso y En espera)
  - [ ] Reglas de base de datos (Flyway, no editar migraciones aplicadas)
  - [ ] Reglas de proceso (ramas, PR, tests obligatorios, no tocar secretos)
  - [ ] Enlaces a los documentos de decisiones técnicas y de funcionamiento
- [ ] Montar CI en GitHub Actions:
  - [ ] Backend: construir y ejecutar tests
  - [ ] Frontend: instalar, lint, tests y build
  - [ ] Construir la imagen Docker del backend en cada PR
  - [ ] (Opcional) levantar `docker compose` y esperar al healthcheck
- [ ] Activar los checks del CI como obligatorios en `main` (después de que hayan corrido una vez)
- [ ] Arreglar el healthcheck:
  - [ ] Permitir `/actuator/health` en Spring Security
  - [ ] Exponer solo `health` en Actuator
  - [ ] Comprobar que `curl` existe en la imagen final
- [ ] Flyway: migración inicial y `ddl-auto=validate`
- [ ] Crear `.env.example` con todas las variables necesarias
- [ ] Decidir la versión de Java

**Hecho cuando:** un PR dispara el CI, no se puede fusionar en rojo y `docker compose up` deja todos los servicios en estado *healthy*.

---

## Sprint 1 · Autenticación y usuarios base

**Objetivo:** login de punta a punta, ya desplegado.

**Base de datos**
- [ ] Migración `organizations` (id, name, active)
- [ ] Migración `users` (id igual al de Supabase Auth, organization_id, role, name, email, active)
- [ ] `CHECK`: `organization_id` obligatorio salvo para el administrador de plataforma
- [ ] `CHECK` sobre los valores permitidos del rol

**Backend**
- [ ] Spring Security como *resource server*: validar el JWT de Supabase (revisar la documentación actual sobre cómo firma los tokens)
- [ ] Enlazar el `sub` del token con `users.id`
- [ ] Endpoint `GET /me` (usuario, rol y organización)
- [ ] CORS configurado por variable de entorno (`localhost` en local, la URL de Vercel en producción)
- [ ] Seed manual: primer administrador de plataforma y primera organización

**Frontend**
- [ ] Login con email/contraseña y Google (`supabase-js`)
- [ ] Rutas protegidas y envío del JWT al backend
- [ ] Pantalla que muestra nombre y rol del usuario

**Tests**
- [ ] Token válido, caducado, ausente y de usuario inexistente
- [ ] Un usuario sin organización (salvo administrador de plataforma) no puede existir

**Despliegue (al cerrar el sprint)**
- [ ] Probar antes la conexión de Render a la base de datos de Supabase (revisar en la documentación actual qué conexión usar desde un servicio externo, por si hace falta el *pooler*)
- [ ] Vercel (frontend) y Render (backend) conectados al repositorio
- [ ] Variables de entorno en cada plataforma
- [ ] URLs de redirección de Google y Supabase configuradas
- [ ] `health check path` configurado en Render

**Hecho cuando:** entras con Google en la URL desplegada y ves tu nombre y tu rol.

**Post sugerido:** login de punta a punta, desplegado.

---

## Sprint 2 · Clientes, sedes e invitaciones

**Objetivo:** que una organización pueda dar de alta a sus clientes y a sus usuarios.

**Base de datos**
- [ ] `clients` (organization_id, name)
- [ ] `sites` (client_id, name, address)
- [ ] `user_sites` (PK compuesta `user_id` + `site_id`)
- [ ] `invitations` (organization_id, email, role, código guardado como *hash*, caducidad, fecha de uso, quién la creó)

**Backend**
- [ ] CRUD de clientes y sedes (administrador de organización y gestor)
- [ ] Crear invitación con rol y, si es cliente, con sus sedes
- [ ] Aceptar invitación: crear el usuario enlazado con Supabase
- [ ] Desactivar usuarios (no se borran)
- [ ] Panel mínimo de administrador de plataforma: crear organización y primer administrador

**Frontend**
- [ ] Pantallas de clientes, sedes y usuarios
- [ ] Flujo de aceptación de invitación

**Tests**
- [ ] Una organización no ve clientes ni sedes de otra
- [ ] Solo los usuarios con rol cliente tienen filas en `user_sites`
- [ ] Las sedes de un usuario cliente son todas del mismo cliente
- [ ] Una invitación no se reutiliza ni se acepta caducada

**Hecho cuando:** un administrador de organización invita a un cliente con tres sedes y esa persona entra y ve su perfil.

---

## Sprint 3 · Incidencias (el núcleo)

**Objetivo:** crear y consultar incidencias con la visibilidad correcta por rol.

**Base de datos**
- [ ] `incidents` (organization_id duplicado a propósito, site_id, created_by, status, title, description, prioridad, fechas)
- [ ] Regla de coherencia: la sede debe ser de la misma organización que la incidencia

**Backend**
- [ ] Crear incidencia (cliente y gestor)
- [ ] Listado y detalle
- [ ] Visibilidad: gestor ve toda su organización, cliente ve las de sus sedes
- [ ] Filtros por estado y sede

**Frontend**
- [ ] Listado con filtros, formulario de creación y detalle

**Tests (los más importantes del proyecto)**
- [ ] Cada rol ve solo lo que le corresponde
- [ ] Un usuario de otra organización recibe 404 o 403
- [ ] No se puede crear una incidencia en una sede de otra organización

**Hecho cuando:** un cliente crea una incidencia, el gestor la ve, y otro cliente no.

**Post sugerido:** cómo se protege el aislamiento entre organizaciones.

---

## Sprint 4 · Equipos y asignación

**Objetivo:** asignar equipos a incidencias con historial.

**Base de datos**
- [ ] `teams` (organization_id, name, active)
- [ ] `users.team_id` (solo técnicos, un único equipo)
- [ ] `assignments` (incident_id, team_id, assigned_by, assigned_at, unassigned_at)
- [ ] Índice único parcial: una sola asignación activa por incidencia

**Backend**
- [ ] Crear equipos y añadir técnicos (gestor)
- [ ] Asignar y reasignar equipo, cerrando la asignación anterior
- [ ] El técnico ve las incidencias de su equipo actual
- [ ] `CHECK`: `team_id` solo para el rol técnico

**Frontend**
- [ ] Gestión de equipos, asignación y vista de incidencias del técnico

**Tests**
- [ ] Un técnico no ve incidencias de otro equipo
- [ ] La asignación anterior se cierra al reasignar
- [ ] El equipo de un técnico es de su misma organización

---

## Sprint 5 · Ciclo de vida y estados

**Objetivo:** transiciones válidas con quién las hace, motivo e historial.

**Base de datos**
- [ ] `status_history` (incident_id, from_status, to_status, changed_by (vacío si es el sistema), reason, created_at)

**Backend**
- [ ] Máquina de estados: Pendiente de asignar, Asignada, En curso, En espera, Finalizada, Cerrada, Cancelada
- [ ] Validar quién puede hacer cada transición
- [ ] Motivo obligatorio en reapertura, cancelación y espera
- [ ] Al reabrir se mantiene el mismo equipo

**Frontend**
- [ ] Botones de acción según rol y estado
- [ ] Línea de tiempo del historial en el detalle

**Tests**
- [ ] Tabla de transiciones permitidas y prohibidas (tests parametrizados)
- [ ] Cerrada y Cancelada son estados terminales
- [ ] Sin motivo no se puede reabrir ni cancelar

**Post sugerido:** el ciclo de vida de la incidencia, con el diagrama de estados.

---

## Sprint 6 · Parte de trabajo y fotos

**Objetivo:** que el técnico rellene el parte desde el móvil.

**Base de datos**
- [ ] `work_reports` (uno por incidencia: diagnóstico, trabajo realizado, material, quién lo rellena, última modificación)
- [ ] `report_photos` (work_report_id, ruta en Storage, descripción)
- [ ] `UNIQUE` sobre `incident_id` en `work_reports`

**Backend**
- [ ] Subida de fotos a Supabase Storage con validación de tipo y tamaño
- [ ] Parte editable en En curso y En espera, de solo lectura en Finalizada
- [ ] No se puede finalizar sin parte completo (definir los datos mínimos)

**Frontend**
- [ ] Formulario del parte pensado para móvil
- [ ] Vista previa del borrador en pantalla

**Tests**
- [ ] El parte no se edita en Finalizada ni en Cerrada
- [ ] No se finaliza sin parte completo
- [ ] Un técnico de otro equipo no accede al parte

---

## Sprint 7 · Informes PDF y cierre automático

**Objetivo:** generar los informes y cerrar solas las incidencias vencidas.

**Base de datos**
- [ ] `final_reports` (uno por incidencia: ruta en Storage, hash, fecha de generación)
- [ ] `document_access_log` (quién vio o descargó qué documento y cuándo)
- [ ] Campo para distinguir cierre por el cliente o por el sistema

**Backend**
- [ ] Elegir la librería de PDF (revisar la licencia si se piensa vender)
- [ ] Borrador generado al vuelo desde el parte, con marca de agua
- [ ] Definitivo generado al cerrar, guardado una sola vez e inmutable
- [ ] Permisos de acceso: el equipo anterior pierde acceso al reasignar
- [ ] Tarea programada: cerrar las Finalizadas con más de 48 h naturales
- [ ] Estrategia para Render gratuito (el servicio se duerme): disparo externo desde GitHub Actions o plan de pago

**Frontend**
- [ ] Ver el borrador, confirmar o reabrir con motivo, y descargar el definitivo

**Tests**
- [ ] El definitivo no se sobrescribe
- [ ] El cierre automático actúa solo sobre las vencidas
- [ ] Cada visualización y descarga queda registrada

**Post sugerido:** la generación del PDF y por qué el borrador no se guarda.

---

## Sprint 8 · Pulido y lanzamiento del MVP

- [ ] Responsive real en móvil
- [ ] Estados de carga y mensajes de error claros
- [ ] Tests E2E con Playwright: login, crear incidencia, finalizar y cerrar
- [ ] Datos de demo
- [ ] README completo con capturas
- [ ] Despliegue estable
- [ ] Prueba con una persona del sector

**Hecho cuando:** una persona ajena al proyecto completa el flujo entero sin ayuda.

---

## Backlog (fuera del MVP)

- IA para asistir en la redacción del parte
- Notificaciones por email
- Comentarios entre cliente y equipo
- Incidencias relacionadas (cuando el problema reaparece tras Cerrada)
- Fotos al crear la incidencia
- Permisos más finos (por ejemplo, gestores por zona)
- Un técnico en varios equipos, con historial de pertenencia

## Decisiones pendientes

- [ ] Versión de Java
- [ ] Librería de PDF
- [ ] Datos mínimos del parte para poder finalizar
- [ ] Qué hacer con el plazo de 48 h cuando una incidencia se reabre y se vuelve a finalizar (propuesta: se reinicia)
- [ ] Si el gestor puede también reabrir una Finalizada
- [ ] Dominio definitivo (de momento, subdominio de `aprokhorenko.dev`)
