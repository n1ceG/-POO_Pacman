# Checklist del TPO

Creado el 27/10/2026 con asistencia de IA a pedido de Juan. Fuente principal: [B02-contratos.md](B02-contratos.md), contrastada con `src/` del commit `942e857`. B02 continúa pendiente de aceptación del grupo; no implica aprobación docente.

## Abrir y usar

Abrir [estado-tpo.html](estado-tpo.html) con doble clic en Chrome, Edge o Firefox. No requiere instalación, conexión, servidor ni bibliotecas externas. Todo el código y el catálogo están dentro del HTML.

Tiene 91 objetivos en nueve secciones: las clases principales, celdas/puntos, tipos auxiliares e integración del equipo. Incluye métodos, reglas, constructores y asociaciones. Las etiquetas N01–N21 enlazan las tarjetas de Trello; abrirlas sí requiere conexión. Las relaciones permiten navegar entre objetos.

Cada regla tiene una casilla independiente y evidencia de la lectura inicial del código. Los métodos parcialmente implementados pueden tener unas reglas marcadas y otras pendientes. Las 55 marcas iniciales representan comportamientos observados en el código, no tarjetas aprobadas ni pruebas completas de integración. Por ejemplo, elegir una ruta de regreso ya existe, aunque la transición `serComido()` que permitiría usarla en una partida normal sigue vacía.

El buscador consulta todos los objetos; sin búsqueda se muestra el objeto seleccionado. Se puede filtrar por marcados, pendientes o decisiones aún abiertas. Los contadores globales incluyen decisiones y documentación: son conteos de objetivos, no un porcentaje de implementación del TPO.

## Guardar y recuperar las marcas

- El HTML conserva las marcas en `localStorage` con clave `poo-pacman-b02-checklist-v1`, dentro del perfil del navegador. No modifica archivos Java ni Trello.
- **Exportar avance** descarga un JSON con todas las casillas y las fechas de edición. Conservar ese JSON fuera de la PC o versionarlo deliberadamente como avance revisado; Git no captura el almacenamiento del navegador.
- **Importar avance** valida el proyecto, la versión y todos los identificadores, pide confirmación y reemplaza las marcas actuales. No fusiona estados de varias personas. Exportar antes si se desea conservar la versión anterior.
- Para otra PC: copiar el HTML y el último JSON, abrir el HTML e importar. Si cambia el navegador, el perfil o la ubicación del archivo, el almacenamiento puede ser diferente; usar el JSON.
- Si el almacenamiento está bloqueado, el HTML lo indica y permite seguir trabajando y exportar. Si encuentra datos ilegibles, no los sobrescribe automáticamente; una importación válida permite reemplazarlos expresamente.

El HTML incluye la base inicial, no una copia viva de las marcas de cada usuario. Las marcas se comparten mediante el JSON. Un cambio en el código Java no actualiza este checklist automáticamente.

## Mantener el catálogo

El bloque `script#catalog` del HTML contiene JSON. Cada objetivo usa la forma:

```text
[id_estable, título, firma, contrato, marcado_inicial, tarjetas_separadas_por_espacios, evidencia_inicial, decisión_pendiente_opcional]
```

Conservar los identificadores para mantener compatibilidad con las exportaciones. Si cambia el conjunto de objetivos, implementar una migración o cambiar versión/clave y explicar cómo recuperar los avances anteriores; la importación actual exige el catálogo completo para evitar pérdidas silenciosas.

Para reiniciar a la base sin tocar el almacenamiento manualmente, usar un perfil de navegador nuevo o importar una exportación de la base previamente conservada. No se agregó un botón de borrado para evitar pérdidas accidentales.

## Verificación del 27/10/2026

Desde la carpeta `TPO-Pacman`, con Node.js instalado solo para la verificación:

```powershell
node docs/verificar-estado-tpo.cjs
```

Comprueba catálogo, cambio de casillas, recarga, exportación/importación, rechazo de datos inválidos, cancelación de importación, funcionamiento sin almacenamiento y conservación de datos ilegibles. Usa un DOM simulado: no sustituye la inspección visual ni comprueba descargas reales.

Se volvió a compilar `src/` con JDK 26.0.2 y `--release 17` en una carpeta temporal, sin modificar Java. Resultados: `TestControlador` 15/16 por la expectativa antigua de inicio quieto; `TestB07` 128/128. Las pruebas no cubren todavía el negocio restante.

La vista previa automática en navegador no pudo abrirse en este entorno: la conexión local agotó el tiempo de espera y el navegador de revisión no permite URLs `file:`. Queda pendiente inspección visual manual al abrir el archivo. El HTML permanece utilizable directamente en el navegador de la PC.
