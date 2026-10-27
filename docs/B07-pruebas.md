# Pruebas B07: movimiento y persecución de fantasmas (R4)

Pruebas y documentación preparadas con asistencia de IA el 27/10/2026.
Archivo ejecutable: [TestB07.java](../src/test/TestB07.java).

## Ejecutar

En VS Code, abrir `src/test/TestB07.java` y pulsar **Run** sobre `main()`.
Es un programa de consola; no requiere JUnit ni la opción `-ea`.

Desde PowerShell, ubicado en la carpeta TPO-Pacman:

```powershell
$fuentesB07 = @(Get-ChildItem -LiteralPath .\src -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -d bin $fuentesB07
if ($LASTEXITCODE -eq 0) {
    java -cp bin test.TestB07
}
```

Cada comprobación muestra [OK] o [FALLA], el resultado esperado y el obtenido.
El resumen cuenta comprobaciones individuales, no mapas: hay diez grupos.
Si alguna falla, el programa termina con AssertionError y código de salida distinto de cero.
El ejecutor continúa con los demás grupos si un caso lanza una excepción.

## Qué comprueba

| Grupo | Evidencia |
| --- | --- |
| Inicio | Cuatro objetos distintos, centro (7,7) del mapa predeterminado y estado PERSIGUIENDO. |
| Integración | Cuatro fantasmas en un mapa pequeño, actualizados a través de ControladorJuego. Una celda por actualización tanto con 0,25 como con 2 segundos. Un segundo escenario distingue perseguir la posición nueva de Pac-Man de perseguir la anterior. |
| Direcciones | Arriba, abajo, izquierda y derecha. Elegir dirección no mueve al fantasma. |
| Pasillo | Recorrido de seis pasos con dos curvas y posiciones esperadas explícitas. |
| Obstáculos | Paredes en las cuatro direcciones y los cuatro bordes de una matriz sin paredes exteriores. |
| Cruce | Tres salidas disponibles: camino de cuatro pasos frente a rodeos de ocho pasos. |
| Callejón | Entra en un extremo cerrado y puede invertir el movimiento para salir. |
| Empates | Rutas de igual longitud y selección repetible según el orden vigente. |
| Sin movimiento | Destino inaccesible y origen igual a destino: dirección null y posición conservada. |
| Partida detenida | Los cuatro conservan su posición cuando actualizar() recibe una partida PAUSADA, en VICTORIA o en DERROTA. |

Los mapas y sus coordenadas están junto a cada caso. Las coordenadas son
`(fila, columna)`, empezando en cero. Los resultados esperados se calcularon
a mano: no se usa caminoMasCorto() para generar la respuesta esperada.

## Comportamientos actuales que estas pruebas fijan

Estas son las reglas observadas en el código vigente; no representan aprobación
docente ni impiden cambios posteriores acordados.

- Callejón: se permite volver por la única salida si existe un camino al objetivo.
  Si el destino es inaccesible, el fantasma permanece quieto.
- Empates: BFS explora los vecinos en el orden ARRIBA, ABAJO, IZQUIERDA, DERECHA.
  Entre caminos igualmente cortos prevalece el descubierto según ese orden.
  La prioridad no reemplaza el criterio de menor longitud.
- Cada actualización recalcula la persecución, incluidos los cruces.
- Partida mueve primero a Pac-Man; luego los fantasmas consultan esa posición.
- Un movimiento avanza como máximo una celda; null conserva la posición.
- Los cuatro pueden ocupar la misma celda y elegir el mismo recorrido.

Si se acuerda modificar alguno de estos contratos, revisar también sus
expectativas en TestB07 antes de interpretar una falla como un error del código.

## Acceso a datos privados y alcance

Partida todavía no ofrece una consulta pública de sus fantasmas. Para comprobar
los cuatro sin agregar métodos al modelo ni cambiar el diagrama, leerFantasmas()
usa reflexión: consulta el campo privado y devuelve una copia del arreglo.
El test no reemplaza ni modifica los elementos de ese arreglo.

fijarEstadoParaPrueba() prepara el campo estado únicamente en las partidas
creadas por el test. Así se prueba la condición de actualizar() que evita
movimientos fuera de EN_CURSO. Esto **no comprueba** que pausar(), reanudar(),
la victoria o la derrota estén implementados. Esos métodos pertenecen a otras
tarjetas. Si cambian los nombres de los campos, habrá que adaptar estos dos
auxiliares. Cuando se acuerden consultas públicas, se podrán usar en su lugar.

La prueba de integración evita llegar al contacto. El susto, el regreso tras
ser comido y las consecuencias de colisiones quedan fuera de B07.

## Resultado registrado

- Fecha: 27/10/2026.
- Compilación: JDK 26.0.2, con `--release 17 -encoding UTF-8`, exitosa.
- Ejecución: `test.TestB07`, código de salida 0.
- Resultado: **128/128 comprobaciones OK**.
- [Salida completa de consola](B07-resultados.txt).
- Base del código revisado: commit local `771ad2e`, más el cambio local que Juan
  tenía en Fantasma.java y estos archivos nuevos. No se creó un commit ni se
  verificó una subida remota como parte de esta tarea.
- No se modificó la lógica del modelo ni el controlador.

Estas pruebas aportan evidencia para B07; no cierran por sí solas los acuerdos
pendientes de B02 ni adjuntan resultados a la tarjeta del tablero. El test previo
TestControlador es independiente y conserva su expectativa antigua de inicio
quieto de Pac-Man, detectada en la revisión anterior.
