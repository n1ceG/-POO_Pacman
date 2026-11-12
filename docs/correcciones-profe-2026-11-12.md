# Correcciones del TPO — 12/11/2026

Fuente: observaciones de la profesora transmitidas por Juan en el chat y código local de `develop`. Análisis e implementación realizados con asistencia de IA. Este documento explica la interpretación aplicada; no constituye una aprobación docente.

## Estado encontrado

Había cambios sin commit en `ControladorJuego`, `Celda`, `Partida`, `Personaje`, `Punto` y `VentanaPrincipal`. Según Juan, la profesora había comenzado a editar el proyecto.

El trabajo estaba incompleto: `Celda` referenciaba atributos inexistentes y tenía un `return` sin terminar; `Personaje` declaraba dos veces `mover(Direccion)`; las subclases de `Punto` seguían sobrescribiendo métodos que ya no existían. No compilaba.

## Qué quedó implementado

| Observación | Implementación y responsabilidad |
| --- | --- |
| Posición debe ser un DTO | `dto.Posicion` transporta fila y columna, sin reglas de movimiento ni referencias a entidades. Ya era esencialmente un contenedor de datos; se formalizó esa función y se reunieron los tres DTO en el paquete `dto`. |
| El controlador no devuelve objetos de negocio | Sus consultas entregan valores, enums compartidos, DTO o matrices de caracteres. No entrega `Partida`, `PacMan`, `Fantasma`, `Laberinto` ni `Celda`. Se conservó la eliminación del constructor público que recibía una `Partida`. |
| Sacar las clases de puntos | Se eliminaron `Punto`, `PuntoComun` y `SuperPunto`. `Celda` conserva un valor y el indicador `fueComido`. |
| Usar `if (valor == 50)` al consumir | `Partida.consumir(int valor)` suma el valor y asusta a los fantasmas cuando vale 50. El valor 0 no produce efectos. |
| `getMapa` corresponde a `Laberinto` | `Laberinto.getMapa()` recorre sus celdas y crea una matriz nueva. `Partida` y `ControladorJuego` delegan la consulta. |
| No pasar el laberinto a `Personaje.mover` | `Partida` calcula el destino, consulta `esTransitable` y llama a `mover(direccion)` únicamente si puede avanzar. Se aplica a PacMan y a los fantasmas. `Personaje` ya no depende de `Laberinto`. |

## Cómo funciona ahora

### Datos para las interfaces

Un DTO transporta información; no ejecuta las reglas del juego. `Posicion` conserva su nombre, aunque esté en `dto`: el sufijo `DTO` no es obligatorio.

`Personaje.getPosicion()` y `Celda.getPosicion()` devuelven un **nuevo** objeto de coordenadas. El controlador entrega esas copias. Aunque una interfaz cambie una coordenada del DTO, no modifica al personaje ni a la celda. Los estados y direcciones son enums compartidos, sin entidades mutables del juego.

`PartidaDTO` y `FantasmaDTO` siguen siendo las representaciones utilizadas por Swing. Las interfaces no importan clases de `modelo`.

### Puntos y consumo

| Contenido de la celda | Valor disponible | Símbolo |
| --- | --- | --- |
| Pared | 0 | `#` |
| Camino vacío o punto consumido | 0 | espacio |
| Punto común | 10 | `.` |
| Superpunto | 50 | `o` |

`Celda.retirarPunto()` devuelve el valor disponible y lo marca como comido. Las siguientes llamadas devuelven 0. El valor configurado permanece guardado, pero `getValor()` devuelve 0 después del consumo. El constructor distingue correctamente pared, camino vacío y puntos; no se conservaron los valores provisionales 50/100 que habían quedado durante la edición.

`Partida` recibe ese número, suma puntaje y decide el efecto sobre los fantasmas. La celda no conoce a los personajes.

### Movimiento y orden del turno

```java
Posicion candidata = pacMan.calcularProximaPosicion(direccion);
if (laberinto.esTransitable(candidata)) {
    pacMan.mover(direccion);
}
```

Este es el contrato de movimiento; `Partida.actualizar()` conserva además las posiciones previstas para detectar intercambios de celdas antes de mover. Se mantuvieron las prioridades existentes: consumo antes del contacto, victoria al comer el último punto y cobro de fantasmas asustados antes de perder una vida por contacto mixto.

El susto conserva diez segundos completos después del turno de consumo. Con Swing, los 50 intervalos posteriores de 200 ms suman diez segundos; en consola son 240 turnos de 1/24 s.

## Validación y límites

- Compilación de **todos** los archivos Java con `--release 17 -encoding UTF-8`, incluidos Swing, consola y pruebas.
- `TestRequerimientos`: **64/64 casos**, **982 comprobaciones** aprobadas.
- `TestCorrecciones`: **46 comprobaciones** aprobadas: DTO sin acceso al estado interno, consultas del controlador, copia del mapa, consumo único, paredes y duración del susto con intervalos de 200 ms.
- La suite de entrega también ejercita la consola mediante entradas simuladas. No se realizó una prueba manual de la ventana Swing.
- Las 18 pruebas anteriores se compararon contra una copia del último commit previo a las ediciones locales. Las mismas nueve suites fallaron antes y después: `TestB07`, `TestConsumo`, `TestControlador`, `TestN10`, `TestN12`, `TestN14`, `TestN17`, `TestN18` y `TestVictoria`. Hay escenarios antiguos que esperan fantasmas superpuestos en el centro o preparan contactos incompatibles con sus posiciones actuales. La coincidencia de suites fallidas no certifica cada comprobación interna de esas suites, algunas aleatorias.
- Las pruebas antiguas se adaptaron a las nuevas firmas para que compilen; no se modificaron sus expectativas históricas para declararlas aprobadas.
- `SoportePruebas` prepara partidas personalizadas mediante reflection exclusivamente dentro de las pruebas, sin reintroducir el constructor público retirado por la profesora.
- Los diagramas UML y documentos anteriores conservan el diseño histórico y necesitan actualización para reflejar estas correcciones. No se modificaron los originales ni se usó el HTML explicativo antiguo como fuente de verdad.

Desde la raíz del proyecto:

```powershell
javac --release 17 -encoding UTF-8 -d bin -sourcepath src src/test/TestRequerimientos.java src/test/TestCorrecciones.java
java -cp bin test.TestRequerimientos
java -cp bin test.TestCorrecciones
```

Si el acceso `javac` de Oracle en el PATH queda bloqueado, se puede invocar directamente el ejecutable del JDK instalado. La verificación de esta sesión usó `C:\Program Files\Java\jdk-26.0.2\bin\javac.exe` y `java.exe`, compilando para Java 17.
