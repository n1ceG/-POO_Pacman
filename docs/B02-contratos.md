# B02 — Contratos entre clases

27/10/2026. Propuesta preparada con asistencia de IA a pedido de Juan. Pendiente de aceptación del grupo; no implica aprobación docente ni del diseño previo.

## Objetivo y fuentes

Un contrato dice qué recibe y devuelve cada método y quién se encarga de cada regla. No exige terminar las implementaciones de otras tarjetas.

Fuentes: src/ sobre commit 771ad2e de develop; README del proyecto; Requerimientos TPO PacMan 1.md; modelo StarUML previo, sujeto a B01/B15; aclaraciones de Juan en la conversación. No se dispone del listado completo vigente de Trello: no se inventan números de tarjetas.

## Responsabilidades y asociaciones

| Clase | Responsabilidad | Relaciones |
| --- | --- | --- |
| ControladorJuego | Recibe órdenes, delega y ofrece consultas a consola/interfaz. | Una Partida. No calcula caminos ni aplica puntaje. |
| Partida | Coordina actualización, consumo, contactos, vidas, puntaje y estado global. | Un Laberinto, un PacMan y cuatro Fantasma. |
| Laberinto | Celdas, paredes, puntos, centro y caminos mínimos. | Celda[][]; no conoce vidas ni controlador. |
| Personaje | Posición actual/inicial, dirección y cálculo de un paso válido. | Abstracta; recibe Laberinto al calcular/mover. |
| PacMan | Guarda dirección elegida y aplica movimiento. | Hereda Personaje. No suma puntos ni descuenta vidas. |
| Fantasma | Elige movimiento según estado, maneja susto y regreso al origen. | Hereda Personaje. No suma los 200 puntos. |
| Celda/Punto | Celda guarda y retira su punto; Punto informa valor. | PuntoComun=10; SuperPunto=50. |

Las asociaciones principales ya existen y los constructores de Partida las inicializan. No agregarlas nuevamente ni crear referencias de regreso al controlador.

## Convenciones propuestas

- Coordenadas (fila, columna), base cero. ARRIBA resta fila, ABAJO suma fila; IZQUIERDA resta columna, DERECHA suma columna.
- Comparar posiciones por coordenadas. Posicion no redefine equals; no usar == para contactos.
- calcularProximaPosicion consulta sin mover; mover aplica como máximo un paso. Dirección null o destino bloqueado conserva posición.
- actualizar representa un paso por personaje; tiempoTranscurrido son segundos para temporizadores, no cantidad de pasos. Fuera de EN_CURSO no avanza nada.
- Posiciones iniciales y actuales independientes. Consultas de personajes devuelven copias.
- Mapas personalizados deben tener inicio y centro transitables. centro() calcula el centro geométrico, no busca una alternativa.
- Una instancia mutable de Laberinto pertenece al escenario de una partida; no compartirla entre partidas que deban ser independientes.

## Constructores existentes

| Firma | Comportamiento actual / precondición propuesta |
| --- | --- |
| ControladorJuego() | Crea new Partida(). |
| ControladorJuego(Partida) | Usa la partida recibida, no nula. |
| Partida() | Mapa nuevo predeterminado, PacMan (13,7), 3 vidas, 0 puntos, EN_CURSO; cuatro fantasmas en centro (7,7). |
| Partida(Laberinto, Posicion) | Mismo estado inicial usando el escenario y posición recibidos. |
| Laberinto() / crearDefault() | Mapa predeterminado nuevo. |
| Laberinto(String[]) | # pared, . punto, o superpunto, espacio celda vacía; valida forma y símbolos. |
| Laberinto(Celda[][]) | Valida matriz rectangular no vacía sin celdas nulas; conserva su referencia. |
| Personaje(Posicion) | Protegido; copia posición inicial y actual. |
| PacMan(Posicion) | Inicia hacia DERECHA. |
| Fantasma(Posicion) | Inicia PERSIGUIENDO. |
| Celda(boolean pared, Punto punto, int fila, int columna) | Permite camino sin punto con null; pared siempre sin punto. |
| Celda(boolean pared, boolean superPunto, int fila, int columna) | Cada camino recibe punto común o superpunto. |

Celda() también existe pero deja posición nula. Evitarlo para escenarios válidos; revisar usos antes de eliminarlo. Las precondiciones de partida/controlador/personajes todavía no se validan uniformemente.

## Firmas que se conservan

Lo siguiente es el contrato propuesto al completar cada tarjeta, no una afirmación de que todo funciona hoy. Tipos sin paquete por legibilidad.

| Clase y firma | Contrato |
| --- | --- |
| ControladorJuego.moverPacMan(Direccion): void | Delega elección; no mueve hasta actualizar. |
| ControladorJuego.actualizar(double): void | Delega en Partida. |
| ControladorJuego.getPosicionPacMan(): Posicion | Copia de posición. |
| ControladorJuego.getVidas()/getPuntaje(): int; getEstado(): EstadoPartida | Consultas delegadas sin modificar negocio. |
| ControladorJuego.pausar()/reanudar(): void | Delegan transiciones a Partida. |
| ControladorJuego.nuevaPartida(): void | Propuesta: reemplaza por Partida predeterminada nueva solo tras VICTORIA/DERROTA. |
| Partida.elegirDireccionPacman(Direccion): void | Delega en PacMan. Respetar la m minúscula del nombre existente. |
| Partida.actualizar(double): void | Coordina movimiento y eventos solo EN_CURSO. |
| Partida.detectarColision(Posicion, List<Posicion>): List<Fantasma> | Propuesta: recibe destinos del tick, lista en orden del arreglo interno. Devuelve fantasmas en contacto, lista vacía si no hay; nunca null. No mueve ni aplica consecuencias. Falta definir contactos por intercambio de celdas. |
| Partida.consumir(Punto): void | null no hace nada; suma valor. SuperPunto además asusta 10 s a quienes no estén REGRESANDO. Retirar de Celda antes de consumir. |
| Partida.resolverColision(Fantasma): void | PERSIGUIENDO causa pérdida de vida; ASUSTADO suma 200 y pasa a REGRESANDO; REGRESANDO no daña ni suma. Una pérdida máxima por tick. |
| Partida.comprobarVictoria(): boolean | Propuesta: consulta pura de !laberinto.quedanPuntos(); actualizar asigna VICTORIA. |
| Partida.perderVida(): void | Descuenta; cero implica DERROTA; si quedan, reinicia personajes. Conserva puntaje y puntos ya consumidos. |
| Partida.pausar()/reanudar(): void | EN_CURSO→PAUSADA y PAUSADA→EN_CURSO; no reabren estados finales. |
| Partida.getPosicionPacMan(): Posicion; getVidas()/getPuntaje(): int; getEstado(): EstadoPartida | Consultas de partida; posición copiada. |
| Laberinto.esTransitable(Posicion): boolean | false ante null, pared o fuera de matriz. |
| Laberinto.obtenerCelda(Posicion): Celda | Celda real o null ante posición nula/fuera de matriz. |
| Laberinto.direccionesDisponibles(Posicion): List<Direccion> | Salidas válidas; lista vacía si ninguna. |
| Laberinto.caminoMasCorto(Posicion, Posicion): List<Posicion> | Incluye origen y destino; lista vacía si imposible; un elemento si coinciden. |
| Laberinto.centro(): Posicion | (filas/2,columnas/2). |
| Laberinto.quedanPuntos(): boolean | Incluye comunes y superpuntos. |
| Personaje.calcularProximaPosicion(Direccion, Laberinto): Posicion | Un paso transitable o copia de posición actual; sin efectos. |
| Personaje.mover(Direccion, Laberinto): void | Subclase aplica un único paso calculado. |
| Personaje.volverAlInicio(): void | Restaura copia del inicio; subclases restablecen su estado de reinicio. |
| PacMan.elegirDireccion(Direccion): void | Dirección persistente. Actualmente admite null; acordar política de entrada. |
| Fantasma.elegirDireccion(Laberinto, Posicion): Direccion | PERSIGUIENDO camino mínimo a PacMan; ASUSTADO salida aleatoria; REGRESANDO camino al inicio. null si no se mueve. |
| Fantasma.asustar(double): void | Ignora REGRESANDO; fija ASUSTADO y reemplaza duración restante, no la acumula. |
| Fantasma.serComido(): void | Propuesta: ASUSTADO→REGRESANDO y limpiar temporizador. |
| Fantasma.actualizarEstado(double): void | Descontar susto; al vencer, PERSIGUIENDO. Al regresar al inicio, PERSIGUIENDO. Coordinar momento de llamada. |
| Fantasma.getEstado(): EstadoFantasma | Consulta sin efectos. |
| Celda.retirarPunto(): Punto | Entrega punto y deja null; no permite cobrarlo dos veces. |

El desempate actual de caminos usa el orden ARRIBA, ABAJO, IZQUIERDA, DERECHA. Es comportamiento del código, no requisito docente verificado.

Consulta adicional pendiente: exponer posiciones/estados de fantasmas a través del controlador mediante copias o views. No existe firma acordada todavía; TestB07 usa reflexión y lo documenta.

## Orden del tick: propuesta para revisar

1. Guarda de EN_CURSO y captura de posiciones anteriores si hacen falta para cruces.
2. Mover PacMan; retirar y consumir punto de llegada. Superpunto reinicia susto a 10 s.
3. Si se agotaron puntos, marcar VICTORIA y terminar antes de aplicar muerte.
4. Elegir y aplicar un paso a cada fantasma hacia la posición nueva de PacMan. El superpunto ya puede influir en su estado.
5. Resolver contactos antes de vencer susto. Una pérdida de vida termina el procesamiento del tick.
6. Actualizar temporizadores y llegada al origen. Propuesta: un susto recién activado comienza a descontarse en el tick siguiente, para no restarle tiempo anterior a su activación.

Hoy actualizar solo hace la guarda y movimientos. Esta secuencia aún no está implementada. Si detectarColision se llama antes de mover fantasmas, usar destinos calculados una sola vez; no recalcular movimiento aleatorio ni mover dos veces.

Prioridades confirmadas por Juan: superpunto antes de contacto; contacto antes de vencimiento; último punto antes de muerte. El orden completo propuesto debe revisarse con el grupo.

## Auditoría de pendientes

| Archivo | Pendiente real |
| --- | --- |
| src/modelo/Partida.java | detectarColision retorna null provisional; comprobarVictoria retorna false provisional. Vacíos: consumir, resolverColision, perderVida, pausar, reanudar. Falta integrarlos y llamar temporizadores desde actualizar. |
| src/modelo/Fantasma.java | serComido y actualizarEstado vacíos. volverAlInicio solo restaura posición. Comentario final sobre movimiento totalmente aleatorio está desactualizado. |
| src/modelo/PacMan.java | volverAlInicio no restaura DERECHA; conservaría la dirección elegida antes de perder vida. |
| src/controlador/ControladorJuego.java | pausar, reanudar y nuevaPartida vacíos. |
| src/modelo/Celda.java | Constructor vacío permite estado incompleto. |
| src/extra/Posicion.java | Mutable, campos públicos, sin igualdad por coordenadas. Cambiar API exige coordinar todos los consumidores. |
| src/modelo/Laberinto.java | Centro no garantiza camino; constructor de celdas comparte matriz. Validar escenarios antes de usarlos. |
| src/test/TestControlador.java | Espera inicio quieto, en contradicción con DERECHA confirmado por Juan y ya implementado. Corregir expectativa al revisar B05. |
| README.md | Su descripción del esqueleto quedó parcialmente desactualizada. |

No todo return null es provisional: obtenerCelda fuera de matriz, retirarPunto vacío y elegirDireccion sin movimiento tienen contratos válidos.

## Archivos por tarjeta

Rutas relativas a src/, salvo docs. Números no disponibles quedan pendientes de cotejo con Trello.

| Tarjeta / trabajo | Archivos principales | Integración |
| --- | --- | --- |
| B01/B15, alcance por cotejar | Diagramas y este acuerdo | Revisar diseño antes de cambiar firmas; conservar originales. |
| B02 | docs/B02-contratos.md, docs/B02-verificacion.txt, enlace en README | Documentación y compilación. |
| B04, laberinto | modelo/Laberinto.java, modelo/Celda.java | API para B07 y consumo. |
| B05, movimiento PacMan | modelo/PacMan.java, modelo/Personaje.java, test/TestControlador.java | Partida: elección y movimiento; controlador delega. |
| B07, fantasmas | modelo/Fantasma.java, test/TestB07.java, docs/B07-* | Partida: cuatro instancias y bucle; Laberinto: caminos. |
| Consumo/puntaje, número por verificar | modelo/PuntoComun.java, modelo/SuperPunto.java, modelo/Celda.java | Partida.consumir y llamada en actualizar. |
| Susto/regreso, número por verificar | modelo/Fantasma.java | Partida.consumir/resolverColision/actualizar; coordinar con B07. |
| Contactos/vidas, número por verificar | modelo/Partida.java, reinicio en PacMan.java/Fantasma.java | Detector, reinicio y corte del tick. |
| Victoria/derrota, número por verificar | modelo/Partida.java y pruebas | Usa Laberinto.quedanPuntos. |
| Pausa/reanudación/nueva partida, número por verificar | controlador/ControladorJuego.java, modelo/Partida.java | Guarda y transiciones de estado. |
| Integración/interfaz, número por verificar | test/ y views si se acuerdan | Usar controlador; consola válida según README para fase B. |

Propuesta: una persona integra Partida.java. Las demás entregan los métodos de su tarjeta y coordinan cambios del constructor/actualizar. B07 y susto comparten Fantasma; B05 y B07 dependen de Personaje. No cambiar firmas sin avisar. Falta asignar nombres de responsables con el grupo.

## Decisiones abiertas

| Tema | Propuesta / dato que falta |
| --- | --- |
| Contacto al intercambiar celdas o entrar en celda de un fantasma que luego sale | No definido en las fuentes disponibles. Elegir política con ejemplos antes del detector. |
| Orden completo del tick | Revisar secuencia anterior y regla del primer descuento del susto. |
| Reinicio tras perder vida | Posiciones y DERECHA confirmados. Propuesta adicional a aceptar: fantasmas PERSIGUIENDO y temporizadores en cero. |
| Consulta pública de fantasmas | Elegir copias/views y firmas para controlador; ajustar B01/B15. |
| Dirección null y tiempos negativos/no finitos | Acordar validación; propuesta: interfaz envía direcciones válidas y segundos finitos no negativos. |
| nuevaPartida con mapa personalizado | Propuesta: vuelve al predeterminado. Si se desea repetir escenario, definir fábrica de mapas. |
| Tablero y responsables | Completar IDs faltantes y designar integrador de Partida. |

B02 permite decisiones abiertas anotadas, pero deben tener responsable/tarjeta antes de empezar el trabajo que dependa de ellas.

## Verificación y cierre

Ver B02-verificacion.txt para resultados medidos. Desde TPO-Pacman:

```powershell
$fuentesB02 = @(Get-ChildItem -LiteralPath .\src -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -d bin $fuentesB02
if ($LASTEXITCODE -eq 0) {
    java -cp bin test.TestControlador
    java -cp bin test.TestB07
}
```

TestControlador imprime fallas sin garantizar código de salida de error; leer su resumen. Compilar no demuestra que las reglas estén completas.

- [x] Revisar src, constructores, asociaciones y retornos provisionales.
- [x] Escribir responsabilidades, contratos y propuesta de archivos.
- [x] Registrar decisiones abiertas y diferencias con diseño previo.
- [ ] Cotejar todas las tarjetas vigentes y asignar integrador.
- [ ] Registrar aceptación o correcciones del grupo.
- [x] Confirmar compilación en la evidencia adjunta: exit 0; TestControlador 15/16 por expectativa antigua; TestB07 128/128.

No se da por acordado el documento por haberlo escrito con IA. Tras aceptación, actualizar esta lista y usarlo como referencia común.

