# **Incidea**

**Incidea** · Documento de decisiones técnicas · v0.1 · Octubre 2026 · Autores: Aleksander Trujillo,

[1\. Glosario](#glosario)

[2\. Roles y permisos](#roles-y-permisos)

[Cliente](#cliente)

[Gestor](#gestor)

[Técnico](#técnico)

[Administrador de organización](#administrador-de-organización)

[Administrador de plataforma](#administrador-de-plataforma)

[3\. Estados](#estados)

[4\. Partes e informes](#partes-e-informes)

[5\. Decisiones pendientes](#decisiones-pendientes)

1. # Glosario {#glosario}

- **Organización**: empresa de mantenimiento que contrata Incidea (tenant).
- **Cliente**: empresa que reporta incidencias a una organización. Puede tener varias sedes y varios usuarios.
- **Sede**: instalación de un cliente donde se produce la incidencia. Cada usuario cliente ve solo las incidencias de sus sedes.
- **Equipo**: grupo de técnicos al que se asigna una incidencia.
- **Parte**: formulario que rellena el técnico (descripción y fotos). De él se generan los informes.
- **Informe**: PDF generado a partir del parte (borrador o definitivo).

2. # Roles y permisos {#roles-y-permisos}

## Cliente {#cliente}

- Ver las incidencias de sus sedes
- Ver el informe borrador mientras la incidencia está **_Finalizada_**, y descargar el definitivo cuando está **_Cerrada_**
- Crear / modificar incidencias (solo puede modificar mientras esté en **_Pendiente de asignar_**)
- Confirmar o reabrir incidencias
- Cancelar incidencia mientras esté en **_Pendiente de asignar_**

## Gestor {#gestor}

- Cancelar incidencias (con motivo)
- Poner incidencias en espera
- Asignar equipos a incidencias
- Ver incidencias
- Cambiar estados de incidencias
- Crear la incidencia
- Descargar el informe una vez finalizado y cerrado
- Ver informe borrador
- Gestionar equipos

## Técnico {#técnico}

- Ver el informe borrador
- Poner **_En espera_** con motivo
- Ver incidencia asignada
- Crear / modificar parte
- Modificar el estado de las incidencia asignadas
- Descargar el informe una vez cerrado
- Ver informe en borrador

## Administrador de organización {#administrador-de-organización}

- Gestión de clientes y sus sedes
- Invitaciones: cada invitación lleva un rol asignado. Un usuario tiene un único rol
- Gestión de técnicos / gestores / clientes

## Administrador de plataforma {#administrador-de-plataforma}

- Crear la organización y su primer administrador
- Gestiona las organizaciones que usan la aplicación

3. # Estados {#estados}

- **Pendiente de asignar**: creada, sin equipo.
- **Asignada**: tiene equipo, aún no ha empezado.
- **En curso**: el equipo está trabajando en la reparación.
- **En espera**: reparación detenida por falta de una pieza o material (se indica el motivo).
- **Finalizada**: Reparada*,* con el parte en modo solo lectura. Empieza el plazo de revisión de 48 horas.
- **Cerrada**: el cliente confirma o vence el plazo. Se genera el informe definitivo. Estado terminal.
- **Cancelada**: creada por error o duplicada. Estado terminal.

Plazo de revisión. Al pasar a **_Finalizada_**, el cliente dispone de 48 horas naturales para **confirmar** o **reabrir la incidencia**. Si no responde, el sistema la cierra automáticamente y queda registrado como cierre automático.

**Reapertura**. Durante el plazo, el **cliente** puede devolverla a En curso con un **motivo obligatorio**. Se mantiene el mismo equipo y el parte vuelve a ser editable. Al finalizar de nuevo se regenera el borrador. Una vez Cerrada, no se puede reabrir.

| De \-\> A                | Quién              | Condición                   |
| :----------------------- | :----------------- | :-------------------------- |
| Pendiente \-\> Asignada  | Gestor             | Elige el equipo             |
| Pendiente \-\> Cancelada | Cliente, gestor    | Motivo                      |
| Asignada \-\> Cancelada  | Gestor             | Motivo                      |
| Asignada \-\> En curso   | Técnico de equipo  |                             |
| En curso \<-\> En espera | Técnico, gestor    | Motivo                      |
| En curso \-\> Finalizada | Técnico del equipo | Parte completo              |
| Finalizada \-\> En curso | Cliente            | Motivo obligatorio          |
| Finalizada \-\> Cerrada  | Cliente o sistema  | Confirmación o vence en 48h |

4. # Partes e informes {#partes-e-informes}

- El informe **nunca se edita a mano**: se genera siempre a partir del parte del técnico.
- **Parte:** se crea cuando el técnico empieza el trabajo. Es editable en En curso y En espera, y de solo lectura en Finalizada. Para corregirlo hay que volver a En curso.
- **Borrador:** desde que existe el parte se puede visualizar un informe borrador, con marca de agua “BORRADOR”. Se genera a partir de los datos actuales del parte, así que siempre refleja la última versión.
- **Definitivo:** se genera al pasar a Cerrada, se guarda una sola vez y es inmutable.
- **Reasignación:** el equipo anterior pierde el acceso al parte y a los informes, y el nuevo lo recibe.
- **Trazabilidad:** se registran las descargas y visualizaciones (quién y cuándo) y los cambios de permisos de acceso.

| ![][image1]            |
| :--------------------- |
| Realizado por: Incidea |

5. # Decisiones pendientes {#decisiones-pendientes}

1. Si se **reasigna** una incidencia en curso, ¿vuelve a Asignada? ¿Se **reinicia el plazo de 48 h** cada vez que se vuelve a finalizar?
1. ¿Puede el **gestor** también reabrir una Finalizada?
1. Definir los **datos mínimos del parte** para poder finalizar.
