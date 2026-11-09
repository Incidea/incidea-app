---
name: org-isolation-review
description: Revisa que el código backend de Incidea respete el aislamiento entre organizaciones, los permisos por rol y las reglas de estado de las incidencias. Úsala siempre que se cree o modifique un endpoint, query, repositorio, servicio o migración que toque datos de organizaciones, usuarios, sedes, equipos, incidencias o PDFs.
---

# Revisión de aislamiento por organización

Un usuario nunca debe ver ni modificar datos de otra organización. Es el fallo más grave del proyecto: trátalo como bloqueante.

## Cuándo aplicar

Antes de dar por terminado cualquier cambio en backend que lea o escriba datos de negocio.

## Proceso

1. **Identifica el alcance.** Lista los endpoints, queries y servicios que ha tocado el cambio.

2. **Aislamiento por organización.** Para cada uno comprueba:
   - La query filtra por `organization_id`, y ese valor sale del usuario autenticado (token + tabla `users`), **nunca** de un parámetro, body o path que envíe el cliente.
   - Los accesos por id (`GET /incidents/{id}`) validan que el recurso pertenece a la organización del usuario. Si no, responde 404, no 403, para no revelar que existe.
   - Los JOIN y subconsultas no cruzan organizaciones.
   - Al crear un registro, `organization_id` se rellena en servidor.
   - Si hay `organization_id` duplicado a propósito (p. ej. en `incidents`), comprueba que no pueda quedar incoherente con el de la entidad relacionada.

3. **Permisos por rol** (cliente, gestor, técnico, administrador de organización, administrador de plataforma):
   - El rol se comprueba en backend (`@PreAuthorize` o equivalente), nunca solo en el front.
   - Cliente: solo ve las sedes de `user_sites`.
   - Técnico: solo ve las incidencias de su equipo.
   - Solo el administrador de plataforma puede existir sin organización y cruzar organizaciones. Revisa que ese camino no se pueda activar por error.
   - Un usuario sin perfil (404 en `/me`) no puede acceder a nada de negocio.

4. **Estados y PDFs.**
   - Las transiciones de estado son solo las permitidas y las hace el rol que corresponde.
   - El parte solo es editable en _En curso_ y _En espera_; en _Finalizada_ es solo lectura y el definitivo es inmutable.
   - Al reasignar, el equipo anterior pierde acceso a la incidencia y a sus PDFs.
   - Los accesos a PDFs quedan registrados.

5. **Tests.** Comprueba que existe, como mínimo, un test por cada endpoint nuevo donde un usuario de la organización A intenta acceder a un recurso de la organización B y recibe 404. Si falta, propón el test.

## Formato de salida

Responde siempre con esta estructura:

- **Veredicto:** OK / CON RIESGOS / BLOQUEANTE
- **Hallazgos:** lista numerada con `archivo:línea`, el problema y por qué es un riesgo.
- **Corrección propuesta:** el cambio concreto, sin aplicarlo salvo que se pida.
- **Tests que faltan:** lista.

No digas que algo está bien si no has leído el código afectado. Si no puedes verificar un punto, dilo.
