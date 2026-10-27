# B02 — Contratos entre clases

27/10/2026. Propuesta preparada con asistencia de IA a pedido de Juan. Pendiente de aceptación del grupo; no implica aprobación docente ni del diseño previo.

## B02 con el formato vigente de las tarjetas

**B02 · Revisar el código y acordar contratos para N01-N21**

**Disponibilidad:** En preparación para revisión del equipo. La auditoría y la compilación están realizadas; falta aceptar contratos propuestos y asignar coordinación de secciones compartidas. No equivale a declarar terminadas sus tareas dependientes.

**Depende de:** No requiere completar otra tarea de código para revisar el estado actual. Consultar la GUÍA y el trabajo existente. B01/B15 pueden provocar ajustes posteriores por devolución docente; no se presupone su aprobación.

**Qué hacer:** Revisar src y documentar responsabilidades, constructores, asociaciones, firmas y resultados esperados. Usar el reparto N01-N21 de este acuerdo para distinguir qué modifica cada tarjeta y cómo integra su resultado. Identificar métodos vacíos y expectativas antiguas de pruebas. Registrar decisiones abiertas y quién debe resolverlas antes de implementar lo que dependa de ellas.

**Terminada cuando:**

- El código compila y existe evidencia de la verificación.
- El equipo dispone de un acuerdo aceptado con responsabilidades y contratos de las clases.
- Cada tarjeta N01-N21 tiene identificados sus archivos y secciones de integración.
- Están identificados los métodos compartidos y acordada su coordinación, respetando responsables actuales.
- Las decisiones aún abiertas están registradas con la tarjeta que las resolverá.
- Se conserva el trabajo existente y se distingue N15 de B15; no se da por aprobado el diseño docente pendiente.

**Archivos:** `docs/B02-contratos.md`, `docs/B02-verificacion.txt` y su enlace en README. Las implementaciones permanecen en sus tarjetas N correspondientes.

**Fuente:** Código local y tablero POO, especialmente GUÍA, consultados el 27/10/2026. Propuesta asistida por IA; aceptación del equipo pendiente.

## Objetivo y fuentes

Un contrato dice qué recibe y devuelve cada método y quién se encarga de cada regla. No exige terminar las implementaciones de otras tarjetas.

Fuentes: src/ sobre commit 771ad2e de develop; README; requerimientos conservados; modelo StarUML previo; aclaraciones de Juan y tablero POO consultado directamente el 27/10/2026. La guía vigente confirma reglas y consolidación. La verificación de Java adjunta pertenece a la revisión anterior de esta misma fecha; esta adaptación documental no ejecutó nuevamente pruebas.

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

## Contratos y archivos por tarjeta vigente

Tablero consultado directamente el 27/10/2026: [POO](https://trello.com/b/uYo0gGU7/poo). Se usa la numeración N01-N21. B02 conserva su identificador porque la consolidación mantuvo su tarjeta e historial; no es N02, que trata la inicialización de vidas y puntaje.

Rutas relativas a `src/`. Las firmas son las existentes revisadas; su asignación por tarjeta es la propuesta de coordinación de B02. Los títulos y la guía fueron consultados en Trello; esta tabla no pretende transcribir todas las descripciones ni certificar su implementación.

| Tarjeta | Archivo y sección que modifica | Contrato que entrega a las demás |
| --- | --- | --- |
| [N01 · Consumir el punto y sumar su valor una sola vez](https://trello.com/c/FovwfART) | `modelo/Partida.java`: `consumir(Punto)` y consumo de celda; `modelo/Celda.java`: `retirarPunto()` si requiere ajuste. | Retirar una sola vez y sumar 10/50; entregar a N09 la detección del superpunto. Coordinar con B06, que sigue En curso. |
| [N02 · Iniciar con tres vidas y puntaje cero](https://trello.com/c/etsOQJ5w) | `modelo/Partida.java`: constructores e inicialización. | 3 vidas, 0 puntos y EN_CURSO en ambos constructores. No reinicia vidas al perder solo una. |
| [N03 · Inicializar cada fantasma en persecución](https://trello.com/c/JScCLSPP) | `modelo/Fantasma.java`: constructor y conservación de origen. | PERSIGUIENDO, tiempo de susto cero, posición inicial independiente de la actual. Coordinar con B07 existente. |
| [N04 · Incorporar cuatro fantasmas en el centro](https://trello.com/c/SzEwG81C) | `modelo/Partida.java`: creación del arreglo en constructores. | Cuatro objetos distintos, mismo centro, sin compartir posiciones mutables. Usa N03; coordinar con B07. |
| [N05 · Elegir camino de persecución](https://trello.com/c/FLDDjhVt) | `modelo/Fantasma.java`: rama PERSIGUIENDO de `elegirDireccion(Laberinto, Posicion)`. | Dirección del primer paso mínimo o null si no hay paso; elegir no mueve. Usa API de Laberinto ya disponible; coordinar con B07. |
| [N06 · Activar y finalizar susto](https://trello.com/c/KCHI0Q71) | `modelo/Fantasma.java`: `asustar(double)`, descuento en `actualizarEstado(double)`. | Reemplaza duración; al vencer vuelve a PERSIGUIENDO; ignora activación en REGRESANDO. N15 coordina momento del descuento. |
| [N07 · Elegir direcciones aleatorias durante susto](https://trello.com/c/oUO0buX4) | `modelo/Fantasma.java`: rama ASUSTADO de `elegirDireccion`. | Solo salidas transitables; null si ninguna. No altera posición al elegir. |
| [N08 · Regresar al origen tras ser comido](https://trello.com/c/0U0dYseq) | `modelo/Fantasma.java`: `serComido()`, rama REGRESANDO y llegada al inicio en `actualizarEstado`. | Pasa de ASUSTADO a REGRESANDO; vuelve por camino válido y recupera PERSIGUIENDO al llegar. Comparte actualizarEstado con N06. |
| [N09 · Activar diez segundos por superpunto](https://trello.com/c/ueQAcZv4) | `modelo/Partida.java`: efecto de SuperPunto en `consumir(Punto)`. | Llama `asustar(10)`; segundo superpunto reinicia, no acumula. N01 conserva suma/retiro; N06 administra el estado individual. |
| [N10 · Mover los cuatro fantasmas](https://trello.com/c/A0DBGBNF) | `modelo/Partida.java`: bucle de fantasmas en `actualizar(double)`. | Un paso máximo por fantasma, solo EN_CURSO; elige según estado. Coordinar código existente de B07 e integración N15. |
| [N11 · Detectar contactos](https://trello.com/c/HLDxnYtT) | `modelo/Partida.java`: `detectarColision(Posicion, List<Posicion>)`. | Devuelve lista de fantasmas en contacto, vacía si ninguno; sin consecuencias. Acordar política de cruces y orden de posiciones con N15 antes de implementarla. |
| [N12 · Descontar vida y resolver reaparición/derrota](https://trello.com/c/QJCyzYKk) | `modelo/Partida.java`: `perderVida()`; `modelo/Fantasma.java`: reinicio; coordinación con PacMan de N21. | Una vida menos; si quedan, reinicia personajes, fantasmas PERSIGUIENDO sin susto/regreso anterior; cero vidas implica DERROTA. Conserva puntaje y puntos consumidos. |
| [N13 · Sumar 200 por fantasma asustado](https://trello.com/c/QrbxxOfb) | `modelo/Partida.java`: rama ASUSTADO de `resolverColision(Fantasma)`. | Suma 200 y llama `serComido()` de N08; no permite cobrar de nuevo por REGRESANDO. |
| [N14 · Resolver contactos persiguiendo o regresando](https://trello.com/c/umOkGAFi) | `modelo/Partida.java`: restantes ramas de `resolverColision`. | PERSIGUIENDO delega en N12; REGRESANDO no produce daño ni premio. Comparte método con N13. |
| [N15 · Integrar colisiones y prioridades](https://trello.com/c/N2221AFa) | `modelo/Partida.java`: orden completo de `actualizar(double)`. | Integra N01/N09/N10/N11/N12/N13/N14/N16 y temporizadores; superpunto antes de contacto, contacto antes de vencimiento y victoria antes de muerte. No reimplementa esos métodos. |
| [N16 · Declarar victoria al agotar puntos](https://trello.com/c/JlWTASR8) | `modelo/Partida.java`: `comprobarVictoria()` y transición VICTORIA, coordinada con N15. | Usa `Laberinto.quedanPuntos()` e incluye superpuntos. Propuesta de consulta pura en comprobarVictoria y asignación desde actualizar, pendiente de acuerdo. |
| [N17 · Pausar y reanudar desde controlador](https://trello.com/c/iGfbJ6k5) | `controlador/ControladorJuego.java` y `modelo/Partida.java`: `pausar()/reanudar()`. | EN_CURSO a PAUSADA y vuelta; sin movimiento ni descuento de susto mientras está pausada. No reabre partida finalizada. |
| [N18 · Nueva partida tras ganar/perder](https://trello.com/c/EXWJS3YQ) | `controlador/ControladorJuego.java`: `nuevaPartida()`. | Reemplaza partida finalizada por escenario nuevo con valores de N02 y dirección de N21; no reutiliza puntos consumidos. Acordar mapa personalizado. |
| [N19 · Pruebas completas por consola](https://trello.com/c/ofnRXwWP) | `test/TestControlador.java` y pruebas adicionales necesarias. | Verifica negocio integrado desde controlador, resultados esperados, bordes, transiciones y aleatoriedad; incluye movimientos, estados, vidas y puntaje. Adaptar expectativa antigua del inicio a N21. |
| [N20 · Instrucciones de ejecución/verificación](https://trello.com/c/DIJsI14b) | `README.md` y documentación de pruebas. | Comandos reproducibles, clases a ejecutar y explicación de resultados/limitaciones. |
| [N21 · Iniciar/reiniciar PacMan hacia DERECHA](https://trello.com/c/abeLd6ev) | `modelo/PacMan.java`: constructor y `volverAlInicio()`; prueba correspondiente. | Dirección DERECHA al crear y reaparecer; movimiento en el siguiente tick válido. Coordinar con B05 y reinicio de N12. |

### Secciones compartidas que hay que coordinar

- `Partida` constructores: N02, N04. `actualizar`: N10 entrega movimiento y N15 integra el tick completo.
- `Partida.consumir`: N01 entrega consumo/puntaje y N09 agrega efecto de superpunto; no escribir dos versiones completas del método.
- `Partida.resolverColision`: N13 y N14 aportan ramas distintas; N12 aporta pérdida de vida.
- `Fantasma.elegirDireccion`: N05, N07 y N08 aportan una rama por estado.
- `Fantasma.actualizarEstado`: N06 controla fin de susto y N08 llegada al origen; N15 decide cuándo llamarlo.
- Reinicio: N12 coordina el conjunto; N21 restaura PacMan y el reinicio de Fantasma cancela estados anteriores.

Propuesta pendiente de asignación: una persona integra cambios de constructor/actualizar en Partida. La guía no fija roles personales: quien toma una tarjeta se agrega como miembro y conserva la responsabilidad hasta terminar. Respetar miembros y estados actuales; no reasignar trabajo por este documento.

### Trabajo previo que se conserva

La guía vigente dice que las tarjetas antiguas en otras listas conservan estado y responsables: B03, B05, B06, B07 y B04 siguen visibles y se usan como avance existente, sin crear implementaciones duplicadas. B07 está asociado a Juan en el tablero consultado; no reutilizar una atribución antigua a Jeremías. Coordinar N03/N04/N05/N10 con B07 y N21 con B05.

B01 (registrar devolución de fase A) y B15 (aplicar correcciones docentes e integrar) siguen bloqueadas. **B15 y N15 son tarjetas distintas:** N15 integra el tick; B15 depende de devolución docente. El acuerdo es revisable cuando llegue esa devolución.

## Decisiones abiertas

| Tema | Propuesta / dato que falta |
| --- | --- |
| Contacto al intercambiar celdas o entrar en celda de un fantasma que luego sale | No resuelto en la guía consultada. N11 debe fijar los ejemplos y N15 aplicar la política; aceptar en B02 antes de implementarla. |
| Orden completo del tick | N15: revisar secuencia propuesta y primer descuento del susto; respetar prioridades confirmadas. |
| Reinicio tras perder vida | Resuelto en la guía vigente: fantasmas PERSIGUIENDO y cancelación de susto/regreso; conservar puntaje/puntos consumidos. Implementación coordinada por N12 y N21. |
| Consulta pública de fantasmas | Elegir copias/views y firmas para controlador en B02, con N19 como consumidor; reflejar los cambios de diseño que correspondan en B01/B15. |
| Dirección null y tiempos negativos/no finitos | Acordar validación; propuesta: interfaz envía direcciones válidas y segundos finitos no negativos. |
| nuevaPartida con mapa personalizado | N18: propuesta de volver al predeterminado. Si se desea repetir escenario, acordar fábrica de mapas. |
| Responsable de integración | Numeración N01-N21 cotejada. Falta que el equipo asigne quién integra las secciones compartidas sin alterar responsables actuales. |

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
- [x] Cotejar N01-N21 y conservar el trabajo previo que sigue en otras listas.
- [ ] Asignar la coordinación de secciones compartidas con el equipo.
- [ ] Registrar aceptación o correcciones del grupo.
- [x] Confirmar compilación en la evidencia adjunta: exit 0; TestControlador 15/16 por expectativa antigua; TestB07 128/128.

No se da por acordado el documento por haberlo escrito con IA. Tras aceptación, actualizar esta lista y usarlo como referencia común.
