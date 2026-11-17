---
name: use-component
description: Úsala siempre que vayas a crear o modificar interfaz (páginas, vistas, formularios, listados, modales, botones, etc.). Obliga a reutilizar los componentes ya existentes del proyecto y prohíbe añadir estilos propios. Si no existe un componente adecuado, hay que preguntar al usuario antes de crear nada.
---

# Usar componentes existentes

## Objetivo

Toda la UI se construye **componiendo los componentes que ya existen** en el proyecto. El aspecto visual lo deciden los componentes y los tokens del sistema de diseño, nunca el agente.

## Dónde buscar

- Componentes: `<src/features/components/>` (y subcarpetas como `ui/`, `common/`, `layout/`)
- Tokens / tema: `<ruta al fichero de tema o variables>`
- Ejemplos de uso: `<stories, páginas existentes, docs>`

## Procedimiento

1. **Inventario primero.** Antes de escribir código, lista y lee los componentes disponibles y sus props/variantes. No asumas que algo no existe sin haberlo buscado (por nombre, por función y en las páginas ya construidas).
2. **Mapea la necesidad a componentes.** Para cada elemento de la interfaz, identifica qué componente existente lo cubre.
3. **Compón, no reinventes.** Usa las props, variantes y slots que el componente ya ofrece (`variant`, `size`, `disabled`, `children`...).
4. **Imita el código existente.** Sigue la forma en que otras páginas o vistas usan esos componentes (imports, estructura, nombres).

## Prohibido

- Estilos inline (`style={{...}}`, `style="..."`).
- Clases utilitarias o CSS nuevo para cambiar el aspecto de un componente (colores, tamaños, márgenes, bordes, sombras, tipografía).
- Valores hardcodeados: colores hex/rgb, píxeles sueltos, fuentes. Si hace falta un valor, debe salir de los tokens del proyecto.
- Copiar y pegar un componente existente para modificarlo en local.
- Instalar librerías de UI nuevas.

> Excepción: el layout básico entre componentes (disposición, separación) solo se permite con los **componentes o utilidades de layout que el proyecto ya define** (`<Stack>`, `<Grid>`, `<Container>`, etc.), nunca con CSS propio.

## Si no existe un componente adecuado

**No lo crees ni improvises estilos. Detente y pregunta al usuario.**

Explica brevemente qué necesitas y por qué ningún componente lo cubre, y ofrece estas opciones (adapta o añade las que apliquen):

- **A. Componer con lo existente:** combinar componentes actuales para aproximarse al resultado. _Ventaja:_ cero código nuevo de UI. _Límite:_ puede no quedar exactamente como se pide.
- **B. Extender un componente existente:** añadir una variante, prop o slot a un componente que ya cubre parte del caso. _Ventaja:_ se mantiene la coherencia y se reutiliza. _Límite:_ toca un componente compartido, hay que revisar que no rompa otros usos.
- **C. Crear un componente nuevo en el sistema de diseño:** en `<ruta de componentes>`, usando solo tokens y siguiendo las convenciones del resto. _Ventaja:_ queda reutilizable. _Límite:_ más trabajo y hay que mantenerlo.
- **D. Solución puntual y local (último recurso):** un componente específico de esa pantalla, claramente marcado como no reutilizable. _Ventaja:_ rápido. _Límite:_ deuda técnica y riesgo de inconsistencia.

Indica **cuál recomiendas y por qué**, y espera la respuesta antes de implementar.

## Formato de la pregunta

> No encuentro un componente para **<qué necesito>**. Estas son las opciones:
> A) ... B) ... C) ... D) ...
> Recomiendo **<opción>** porque <motivo>. ¿Cuál prefieres?

## Antes de terminar

Revisa tu diff y comprueba:

- [ ] Todos los elementos de UI usan componentes existentes.
- [ ] No hay estilos inline, CSS nuevo ni valores hardcodeados.
- [ ] Cualquier componente nuevo o extensión fue aprobado por el usuario.
