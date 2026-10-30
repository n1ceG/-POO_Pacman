# Pac-Man - preparación para la fase B

## Pruebas de los 10 requerimientos para entregar

El archivo único de pruebas es [src/test/TestRequerimientos.java](src/test/TestRequerimientos.java),
preparado con asistencia de IA a pedido de Juan. Su fuente es
[Requerimientos TPO PacMan 1.md](Requerimientos%20TPO%20PacMan%201.md).
Es independiente de los tests anteriores: no los invoca ni necesita compilarlos.

Con JDK 17 o superior, desde la carpeta `TPO-Pacman`:

```powershell
javac --release 17 -encoding UTF-8 -d bin -sourcepath src src/test/TestRequerimientos.java
if ($LASTEXITCODE -eq 0) { java -cp bin test.TestRequerimientos }
```

También se puede ejecutar el `main` de `TestRequerimientos` desde VS Code o IntelliJ.
No requiere JUnit ni dependencias externas, habilitar assertions, ingresar teclas
o esperar diez segundos reales. La compilación anterior toma automáticamente las
clases del juego que necesita, incluyendo la consola, y omite los tests anteriores.
Para entregar, incluir `src/modelo`, `src/extra`, `src/controlador`, `src/consola`
y únicamente `src/test/TestRequerimientos.java` como archivo de pruebas.

La ejecución muestra `[OK]` o `[FALLO]` por caso y un resumen para cada requerimiento.
Ante un fallo informa la comprobación y, cuando corresponde, los valores esperado
y obtenido; continúa con los otros casos. El proceso devuelve código 0 si todos
pasan y 1 si hay fallos. Las pruebas anteriores se conservan como material de
desarrollo y no forman parte de esta suite de entrega.

| Requerimiento | Qué se comprueba |
| --- | --- |
| 1. Movimiento | Cuatro direcciones, giros, continuidad, una celda por tick, inicio hacia la derecha y teclas de consola. |
| 2. Paredes | Bloqueo en las cuatro direcciones, bordes del mapa y elección de una salida libre. |
| 3. Puntaje | Puntos comunes de 10, cuatro superpuntos de 50 en esquinas, consumo único, acumulación y consultas sin modificar el juego. |
| 4. Enemigos | Cuatro fantasmas, posiciones iniciales propias, avance autónomo, elección en cruces, rodeos y ausencia de camino. |
| 5. Superpunto | Susto de diez segundos, 240 ticks, renovación, pasos aleatorios legales, exclusión de regresando y prioridad sobre contacto. |
| 6. Consumo de fantasmas | 200 por fantasma, varios contactos, cobro único, regreso al origen propio, reactivación e inocuidad mientras regresa. |
| 7. Muerte | Misma llegada, intercambio de celdas, ausencia de falsos contactos, contactos múltiples y mixtos, reinicio y conservación del progreso. |
| 8. Vidas | Tres vidas iniciales, tres colisiones hasta derrota, detención tras perder y mínimo de cero vidas. |
| 9. Victoria | Espera hasta consumir comunes y superpuntos, último punto, prioridad sobre muerte y detención al ganar. |
| 10. Control | Pausa de movimiento y temporizador, reanudación, reinicios permitidos/rechazados, restauración e independencia de partidas, comandos y salida. |

Los escenarios se crean dentro del mismo archivo. Algunos separan a PacMan de
los fantasmas y dejan un punto de reserva inaccesible para aislar una regla sin
terminar prematuramente la partida. Otros preparan contactos mediante movimientos
legales. La única lectura por reflection obtiene los fantasmas de la partida;
no escribe campos privados ni fuerza estados finales. Victoria y derrota se
alcanzan consumiendo puntos o recibiendo colisiones mediante el controlador.
Los asustados se prueban por movimientos legales; no se exige una secuencia ni
una distribución aleatoria determinada. Para los contactos se usa un pasillo con
una única salida, de modo que el resultado no dependa del azar.

Las secciones siguientes conservan el historial de preparación y desarrollo.

Estructura preparada con asistencia de IA a pedido de Juan el 22/10/2026. Se replicó el formato de `Partida.java`, preparado por Juan: paquetes, imports, atributos, firmas de métodos y cuerpos vacíos o retornos provisionales. La lógica será desarrollada por Juan y su grupo.

## Alcance y fuentes

- La página 2 de [la consigna docente](TPO_PacMan_Enunciado.pdf) define la fase B como "Código del negocio funcionando con controlador y test". La interfaz gráfica corresponde a la entrega final.
- Juan confirmó que [DiagramaClasesTPO_1.mdj](StarUML/DiagramaClasesTPO_1.mdj) es la referencia previa a la devolución docente. El diseño sigue pendiente de correcciones; esta preparación no implica aprobación.
- [MuevoObjetos](../MuevoObjetos/) es el ejemplo que Juan identifica como aportado por el profesor. Se toma su separación entre modelo, controlador y prueba de consola. Su configuración utiliza Java 17.
- Según lo explicado por Juan, las pruebas deberán verificar que los valores y resultados sean razonables, siguiendo la idea de `MuevoObjetos/src/test/TestControlador.java`.

## Abrir en VS Code

1. Usar **Archivo > Abrir carpeta** y seleccionar esta carpeta `TPO-Pacman`. Así se aplica su configuración propia, sin mezclar los ejercicios de otras carpetas.
2. Tener instalado un JDK y la extensión **Extension Pack for Java**, recomendada en `.vscode/extensions.json`.
3. Abrir los archivos de `src`. VS Code utiliza `src` como raíz de los paquetes y `bin` para los compilados.

No se requieren bibliotecas externas, Maven ni Gradle. El esqueleto se verificó con JDK 26.0.2 compilando con `--release 17`, compatible con la versión declarada por el ejemplo. La configuración no fija una ruta de JDK exclusiva de esta PC.

## Archivos para desarrollar

```text
src/
  controlador/
    ControladorJuego.java
  extra/
    Direccion.java
    EstadoFantasma.java
    EstadoPartida.java
    Posicion.java
  modelo/
    Celda.java
    Fantasma.java
    Laberinto.java
    PacMan.java
    Partida.java
    Personaje.java
    Punto.java
    PuntoComun.java
    SuperPunto.java
  test/
    TestControlador.java
```

`modelo` contiene el negocio; `controlador`, su coordinación; `extra`, posiciones y enumeraciones; y `test`, la futura prueba del controlador. Se normalizó el nombre previo `controller` a `controlador`, como en el ejemplo.

`Personaje` y `Punto` son abstractas. `PacMan` y `Fantasma` extienden `Personaje`; `PuntoComun` y `SuperPunto` extienden `Punto`. Las tres enumeraciones incluyen los valores declarados en el StarUML. El archivo contiene tanto clases como enumeraciones con esos nombres: se preparó un único `enum` Java por nombre.

Los atributos y las firmas del resto de las clases siguen `StarUML/DiagramaClasesTPO_1.mdj`, convirtiendo `bool` a `boolean`. Se mantuvo intacto el archivo `Partida.java` de Juan. No se agregaron constructores ni atributos derivados de asociaciones que no estuvieran declarados como atributos en el modelo. `Punto.valor()` es abstracto; sus implementaciones y los demás métodos con retorno usan `0`, `false` o `null` únicamente para compilar. Incluso los getters quedan pendientes de implementar. Los métodos `void` y el `main` de `TestControlador` están vacíos.

Las clases de `views` se definirán si hacen falta para transportar datos. No se anticiparon clases de interfaz gráfica ni conversiones a views. Se preservaron los diagramas, los requerimientos y el ejemplo docente.

## Compilar y ejecutar el esqueleto

Desde una terminal PowerShell abierta en esta carpeta, con `javac` disponible:

```powershell
$fuentesTpo = @(Get-ChildItem -LiteralPath .\src -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -d bin $fuentesTpo
```

El esqueleto compila. `TestControlador` incluye un `main` vacío: ejecutarlo termina sin mostrar resultados ni verificar comportamientos. Para desarrollar las pruebas, completar ese método y ejecutarlo con **Run** en VS Code o con:

```powershell
java -cp bin test.TestControlador
```

Los archivos de `bin` se regeneran al compilar y no se versionan. Esta estructura no representa la fase B terminada.

## Revisión B02 — 27/10/2026

El código avanzó respecto del esqueleto descrito arriba. Consultar [contratos y auditoría de B02](docs/B02-contratos.md) y [verificación actual](docs/B02-verificacion.txt). El documento distingue implementaciones existentes, propuestas y decisiones pendientes de aceptación del grupo. La compilación fue exitosa; TestControlador conserva una expectativa antigua incompatible con el inicio hacia DERECHA.



## Checklist interactivo del TPO

Abrir [estado-tpo.html](docs/estado-tpo.html) para revisar y marcar los contratos de B02 por clase y comportamiento. Incluye relaciones entre objetos y exportacion/importacion de las marcas. Consultar [uso, fuentes y recuperacion](docs/estado-tpo-uso.md). Las marcas iniciales provienen de una lectura del codigo del 27/10/2026 y no equivalen a tarjetas aprobadas.

## Juego por consola — 29/10/2026

Implementación de consola y pruebas preparadas con asistencia de IA a pedido de Juan.
Esta sección describe el estado actual; las secciones anteriores sobre el esqueleto son históricas.

Desde PowerShell, ubicado en `TPO-Pacman`, con un JDK 17 o superior:

```powershell
$fuentesTpo = @(Get-ChildItem -LiteralPath .\src -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -d bin $fuentesTpo
if ($LASTEXITCODE -eq 0) { java -cp bin consola.JuegoConsola }
```

También se puede ejecutar el `main` de `src/consola/JuegoConsola.java` desde VS Code.
Usar la terminal para ingresar comandos.

| Entrada (seguida de Enter) | Acción |
| --- | --- |
| W / A / S / D | Elegir arriba / izquierda / abajo / derecha y avanzar un tick. |
| Enter solo | Avanzar un tick manteniendo la dirección. |
| P | Pausar o reanudar, sin avanzar. |
| N | Nueva partida solo tras victoria o derrota. |
| Q | Salir. |

Se acepta mayúscula o minúscula. Los comandos inválidos, la pausa y los estados finales no avanzan ticks ni cambian dirección. El fin de entrada cierra el juego.

Cada tick representa `1.0 / 24.0` segundos simulados y mueve cada personaje como máximo una celda. No se ejecutan automáticamente 24 ticks por segundo real: se espera un comando por tick. Pensar o escribir no consume tiempo del juego. El susto conserva 10 segundos al consumir un superpunto y vence después de 240 ticks posteriores; se tolera el error mínimo de redondeo de `double`.

La consola muestra `P` (PacMan), `F` (perseguidor), `f` (asustado), `r` (regresando), `#` (pared), `.` (punto) y `o` (superpunto). Si varios personajes se superponen, el tablero muestra un solo símbolo; debajo figuran los cuatro fantasmas con posición y estado. Antes de dibujar cada estado se limpia la pantalla mediante secuencias ANSI. Ejecutar en una terminal compatible, como la terminal integrada de VS Code o Windows Terminal.

`JuegoConsola` lee y presenta; `ControladorJuego` coordina las acciones del jugador; `Partida` conserva las reglas. `getMapa()` devuelve una matriz nueva de símbolos sin exponer celdas mutables. En una colisión mixta se cobran todos los asustados tocados y después se pierde una sola vida, independientemente del orden de los fantasmas. Se mantienen las prioridades previas de superpunto antes de contacto y victoria antes de muerte.

### Separación entre consola y controlador

Refactor realizado con asistencia de IA a pedido de Juan. El punto de entrada sigue siendo `consola.JuegoConsola` y los comandos se mantienen.

- `JuegoConsola` es la interfaz entre el jugador y el controlador: lee las letras, las convierte en acciones y muestra el tablero, el estado y los mensajes. Conserva el ciclo de lectura y la limpieza de pantalla porque pertenecen a la consola.
- `ControladorJuego.avanzarTurno(direccion)` elige la dirección y avanza una sola vez; `avanzarTurno()` conserva la dirección actual. Cada turno usa `1.0 / 24.0` segundos. En pausa o al finalizar, el controlador impide tanto avanzar como cambiar la dirección.
- `alternarPausa()` decide si corresponde pausar o reanudar. `nuevaPartida()` devuelve `true` si creó la partida y `false` si la rechazó; la consola usa ese resultado únicamente para informar al jugador.
- El controlador lleva los ticks y el tiempo simulado, y los reinicia al crear otra partida. Se conservan `moverPacMan()` y `actualizar(segundos)` para las pruebas y otros clientes: elegir dirección no avanza; cada actualización aceptada cuenta un tick y suma los segundos recibidos.
- `Partida.actualizar()` sigue resolviendo movimientos, consumo, colisiones, vidas y victoria. Estas reglas pertenecen al modelo y no se trasladan al controlador.

La consola ya no compara puntajes y vidas antes y después de cada acción para generar mensajes adicionales: los totales se muestran en el encabezado. El controlador no conoce letras, `Scanner`, símbolos ni mensajes de consola, por lo que puede reutilizarse desde otra interfaz.

Prueba de turnos, pausa, estados finales y reinicio: `java -cp bin test.TestTurnosControlador`.

Verificación del refactor: compilación con `--release 17`, prueba nueva y secuencia de comandos por consola correctas (avance, entrada inválida, pausa, dirección durante pausa, reinicio rechazado, reanudación y salida). En la comparación antes/después sobre la copia de trabajo, conservaron fallos `TestB07`, `TestConsumo`, `TestControlador`, `TestN10`, `TestN12`, `TestN14`, `TestN18` y `TestVictoria`. Ambas ejecuciones incluyeron el cambio local previo de posiciones iniciales en `Partida`; varias pruebas todavía esperan fantasmas superpuestos en el centro. Esta verificación no declara aprobada la suite completa ni modifica ese cambio previo.

Pruebas de la copia del mapa y los 240 ticks:

```powershell
java -cp bin test.TestConsola
java -cp bin test.TestN15
```

Para ejecutar todas las pruebas después de compilar:

```powershell
Get-ChildItem .\src\test -Filter *.java | ForEach-Object {
    java -cp bin ("test." + $_.BaseName)
    if ($LASTEXITCODE -ne 0) { throw ("Falló " + $_.BaseName) }
}
```

Limitaciones conocidas: los perseguidores pueden coincidir por seguir el mismo camino; elegir una dirección contra una pared detiene a PacMan; los constructores de escenarios personalizados presuponen inicios transitables. No se modificaron estas reglas al agregar la consola.
