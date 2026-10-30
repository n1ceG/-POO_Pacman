# Pac-Man — TPO de Paradigma Orientado a Objetos

Juego desarrollado en Java para la fase de lógica de negocio, controlador y pruebas. Se juega por consola, avanzando un turno con cada entrada del jugador.

## El juego

PacMan debe recorrer el laberinto y comer todos los puntos para ganar. Comienza con tres vidas y lo persiguen cuatro fantasmas. Los puntos comunes suman 10 puntos y los superpuntos, 50. Un superpunto asusta a los fantasmas durante 10 segundos de juego; comer un fantasma asustado suma 200 puntos. La partida termina al completar el laberinto o perder las tres vidas.

## Cómo ejecutarlo

Requiere **JDK 17 o superior**, sin bibliotecas externas. Desde una terminal ubicada en `TPO-Pacman`:

```sh
javac --release 17 -encoding UTF-8 -d bin -sourcepath src src/consola/JuegoConsola.java
java -cp bin consola.JuegoConsola
```

También se puede abrir la carpeta en un IDE y ejecutar el `main` de `src/consola/JuegoConsola.java`. Usar una terminal compatible con ANSI, como la integrada de VS Code o Windows Terminal, para que se limpie la pantalla.

## Controles

Todas las letras se ingresan seguidas de **Enter**.

| Entrada | Acción |
| --- | --- |
| W / A / S / D | Mover arriba / izquierda / abajo / derecha y avanzar un turno. |
| Enter solo | Avanzar un turno manteniendo la dirección. |
| P | Pausar o reanudar. |
| N | Iniciar otra partida después de ganar o perder. |
| Q | Salir. |

Cada turno representa 1/24 de segundo de juego. El tiempo avanza al jugar un turno; esperar entre comandos no lo modifica.

En el tablero: `P` es PacMan; `F`, un fantasma perseguidor; `f`, uno asustado; `r`, uno regresando; `#`, una pared; `.`, un punto; y `o`, un superpunto.

## Pruebas

`src/test/TestRequerimientos.java` reúne las pruebas de los 10 requerimientos. Para ejecutarlas:

```sh
javac --release 17 -encoding UTF-8 -d bin -sourcepath src src/test/TestRequerimientos.java
java -cp bin test.TestRequerimientos
```

Muestra `[OK]` o `[FALLO]` por caso y un resumen final. No necesita JUnit ni intervención del usuario. Los demás tests son pruebas de desarrollo y no forman parte de la entrega.

## Organización

- `src/modelo`: personajes, laberinto y reglas de la partida.
- `src/controlador`: coordinación del juego y acciones del jugador.
- `src/consola`: entrada por teclado y presentación del tablero.
- `src/extra`: posiciones, direcciones y estados.
- `src/test/TestRequerimientos.java`: pruebas para la entrega.

La consigna y los diagramas se incluyen en el proyecto. El historial de desarrollo y la procedencia de los aportes asistidos por IA se conservan en `docs/README-desarrollo.md`.
