"""Secuencias leídas del código Java. Elaboración asistida por IA, 30/10/2026.

Los mensajes son llamadas reales; las asignaciones y omisiones se expresan en notas.
Las secuencias son vistas parciales del mismo tick, no ejecuciones independientes.
"""

def m(a, b, text):
    return {"kind": "message", "from": a, "to": b, "text": text}

def r(a, b, text):
    return {"kind": "return", "from": a, "to": b, "text": text}

def n(text):
    return {"kind": "note", "text": text}

def f(op, *branches):
    return {"kind": "fragment", "op": op, "branches": [
        {"guard": guard, "steps": steps} for guard, steps in branches]}


SEQUENCES = [
    {
        "id": "R01", "name": "Movimiento", "participants": [
            ("vista", "JuegoConsola"), ("control", "ControladorJuego"),
            ("partida", "Partida"), ("pac", "PacMan"), ("lab", "Laberinto")],
        "sources": ["JuegoConsola.procesar", "ControladorJuego.avanzarTurno",
                    "ControladorJuego.moverPacMan", "ControladorJuego.actualizar",
                    "Partida.elegirDireccionPacman", "Partida.actualizar", "PacMan.elegirDireccion",
                    "PacMan.mover", "Personaje.calcularProximaPosicion"],
        "steps": [
            n("Entrada: W/A/S/D + Enter. Partida EN_CURSO, quedan puntos y el tick no termina por victoria ni muerte."),
            m("vista", "vista", "procesar(comando)"),
            m("vista", "control", "avanzarTurno(direccion)"),
            m("control", "partida", "getEstado()"),
            r("partida", "control", "EN_CURSO"),
            m("control", "control", "moverPacMan(direccion)"),
            m("control", "partida", "getEstado()"),
            m("control", "partida", "elegirDireccionPacman(direccion)"),
            m("partida", "pac", "elegirDireccion(direccion)"),
            n("PacMan guarda direccion. El cambio de dirección no produce movimiento por sí solo."),
            m("control", "control", "avanzarTurno()"),
            m("control", "control", "actualizar(1.0 / 24.0)"),
            m("control", "partida", "getEstado()"),
            m("control", "partida", "actualizar(tiempoTranscurrido)"),
            m("partida", "pac", "getDireccion()"),
            m("partida", "pac", "calcularProximaPosicion(direccionPacMan, laberinto)"),
            m("pac", "lab", "esTransitable(candidata)"),
            r("lab", "pac", "boolean"),
            r("pac", "partida", "proximaPosicionPacMan"),
            n("Aquí ocurren consumo y posible victoria (R3/R5/R9); después se preparan fantasmas y colisiones (R4/R7)."),
            m("partida", "pac", "mover(direccionPacMan, laberinto)"),
            m("pac", "pac", "calcularProximaPosicion(direccion, laberinto)"),
            m("pac", "lab", "esTransitable(candidata)"),
            n("PacMan asigna la posición calculada: una celda si es transitable; en otro caso conserva su celda (R2)."),
            r("partida", "control", "fin de actualizar"),
            n("Controlador: ticks++; tiempoSimulado += tiempoTranscurrido. Enter sin letra llama avanzarTurno() y conserva direccion.")]
    },
    {
        "id": "R02", "name": "Paredes", "participants": [
            ("partida", "Partida"), ("pac", "PacMan"), ("lab", "Laberinto"), ("celda", "Celda")],
        "sources": ["Partida.actualizar", "PacMan.mover", "Personaje.calcularProximaPosicion",
                    "Laberinto.esTransitable", "Laberinto.obtenerCelda", "Celda.esPared"],
        "steps": [
            n("Detalle del movimiento en una grilla de celdas enteras. No hay coordenadas continuas ni un método adicional de alineación."),
            m("partida", "pac", "calcularProximaPosicion(direccionPacMan, laberinto)"),
            n("Método heredado de Personaje: candidata = posición actual desplazada una celda. Con direccion == null no desplaza."),
            m("pac", "lab", "esTransitable(candidata)"),
            m("lab", "lab", "obtenerCelda(posicion)"),
            f("alt",
              ("posición nula o fuera del mapa", [r("lab", "lab", "null"), r("lab", "pac", "false")]),
              ("posición dentro del mapa", [r("lab", "lab", "celda"), m("lab", "celda", "esPared()"),
                r("celda", "lab", "pared"), r("lab", "pac", "!pared")])),
            f("alt",
              ("esTransitable == true", [r("pac", "partida", "candidata")]),
              ("esTransitable == false", [r("pac", "partida", "copia de la posición actual")])),
            n("Si el tick continúa, Partida procesa los demás eventos antes de confirmar el movimiento."),
            m("partida", "pac", "mover(direccionPacMan, laberinto)"),
            m("pac", "pac", "calcularProximaPosicion(direccion, laberinto)"),
            n("Se repite la misma validación y se asigna posicion. Una pared impide avanzar; la dirección elegida queda guardada.")]
    },
    {
        "id": "R03", "name": "Puntaje", "participants": [
            ("partida", "Partida"), ("pac", "PacMan"), ("lab", "Laberinto"),
            ("celda", "Celda"), ("punto", "Punto")],
        "sources": ["Partida.actualizar", "Partida.consumir", "Laberinto.obtenerCelda",
                    "Celda.retirarPunto", "PuntoComun.valor", "SuperPunto.valor"],
        "steps": [
            n("Inicio de actualizar(): EN_CURSO y comprobarVictoria() == false. La construcción de Laberinto(String[]) coloca los puntos del mapa."),
            m("partida", "pac", "getDireccion()"),
            m("partida", "pac", "calcularProximaPosicion(direccionPacMan, laberinto)"),
            r("pac", "partida", "proximaPosicionPacMan"),
            m("partida", "lab", "obtenerCelda(proximaPosicionPacMan)"),
            r("lab", "partida", "celdaDestino"),
            f("opt", ("celdaDestino != null", [
                m("partida", "celda", "retirarPunto()"),
                n("Celda guarda el punto retirado y asigna punto = null; por eso no se cobra dos veces."),
                r("celda", "partida", "puntoConsumido (o null)"),
                m("partida", "partida", "consumir(puntoConsumido)"),
                f("alt", ("punto == null", [n("consumir() retorna sin cambiar el puntaje.")]),
                  ("punto != null", [m("partida", "punto", "valor()"),
                    r("punto", "partida", "10 si PuntoComun; 50 si SuperPunto"),
                    n("Partida: puntaje += valor. Si es SuperPunto, asusta a los fantasmas (R5).")]))])),
            m("partida", "partida", "comprobarVictoria()"),
            n("El consumo ocurre antes de mover y de resolver colisiones. Un último punto gana inmediatamente (R9); si continúa, se procesa R4/R7.")]
    },
    {
        "id": "R04", "name": "Enemigos", "participants": [
            ("partida", "Partida"), ("fantasma", "Fantasma"), ("lab", "Laberinto")],
        "sources": ["Partida.actualizar", "Fantasma.elegirDireccion", "Fantasma.mover",
                    "Personaje.calcularProximaPosicion", "Laberinto.caminoMasCorto",
                    "Laberinto.vecino", "Laberinto.reconstruirCamino"],
        "steps": [
            n("Detalle de un tick que continúa tras consumir. Hay cuatro fantasmas; esta rama muestra PERSIGUIENDO. Destino: próxima posición de PacMan."),
            f("loop", ("i = 0 .. 3: preparar direcciones y posiciones", [
                m("partida", "fantasma", "elegirDireccion(laberinto, proximaPosicionPacMan)"),
                m("fantasma", "lab", "caminoMasCorto(posicion, posicionPacman)"),
                n("Laberinto ejecuta BFS. Si origen/destino no son transitables devuelve lista vacía; si coinciden, lista de un elemento."),
                f("loop", ("BFS: mientras haya pendientes y no se alcance destino", [
                    m("lab", "lab", "vecino(actual, direccion)"),
                    m("lab", "lab", "esTransitable(proxima)"),
                    n("Recorre Direccion.values(): ARRIBA, ABAJO, IZQUIERDA, DERECHA. Marca visitado, padre y encola vecinos libres.")])),
                f("opt", ("BFS alcanza destino", [m("lab", "lab", "reconstruirCamino(padre, origen, destino)")])),
                r("lab", "fantasma", "camino (o lista vacía si no existe)"),
                n("Fantasma usa camino[1] para elegir dirección. Si camino tiene 0 o 1 elementos devuelve null. Se calcula en cada tick, también fuera de cruces."),
                r("fantasma", "partida", "direccionFantasma"),
                m("partida", "fantasma", "calcularProximaPosicion(direccionFantasma, laberinto)"),
                r("fantasma", "partida", "proximaFantasma")])),
            m("partida", "partida", "detectarColision(proximaPosicionPacMan, proximasPosicionesFantasmas)"),
            n("Se detecta antes de mover; después Partida mueve a PacMan (R1/R7)."),
            f("loop", ("cada uno de los cuatro fantasmas", [
                m("partida", "fantasma", "mover(direccionesFantasmas[i], laberinto)"),
                m("fantasma", "fantasma", "calcularProximaPosicion(direccion, laberinto)"),
                m("fantasma", "lab", "esTransitable(candidata)"),
                n("Fantasma asigna posicion. Una celda por tick; null o pared conserva su posición.")])),
            n("Después se resuelven contactos (R6/R7). Si no pierde vida, se actualizan estados (R5/R6). El desempate del BFS sigue el orden del enum.")]
    },
    {
        "id": "R05", "name": "Superpunto", "participants": [
            ("partida", "Partida"), ("punto", "SuperPunto"), ("fantasma", "Fantasma"),
            ("lab", "Laberinto"), ("azar", "Random")],
        "sources": ["Partida.consumir", "Partida.actualizar", "SuperPunto.valor", "Fantasma.asustar",
                    "Fantasma.elegirDireccion", "Fantasma.actualizarEstado", "Laberinto.direccionesDisponibles"],
        "steps": [
            n("Celda.retirarPunto() ya devolvió un SuperPunto (R3). Se muestra un tick sin victoria ni pérdida de vida."),
            m("partida", "partida", "consumir(puntoConsumido)"),
            m("partida", "punto", "valor()"), r("punto", "partida", "50"),
            n("Partida suma 50 y reconoce punto instanceof SuperPunto."),
            f("loop", ("cada fantasma", [m("partida", "fantasma", "asustar(DURACION_SUSTO)"),
                f("alt", ("estado == REGRESANDO", [n("asustar() retorna; conserva REGRESANDO.")]),
                  ("otro estado", [n("estado = ASUSTADO; tiempoSustoRestante = 10. Otro superpunto reinicia el valor en 10.")]))])),
            n("actualizar() registra sustoRenovado = true antes de elegir direcciones y resolver contactos."),
            f("loop", ("cada fantasma ASUSTADO durante la preparación del movimiento", [
                m("partida", "fantasma", "elegirDireccion(laberinto, proximaPosicionPacMan)"),
                m("fantasma", "lab", "direccionesDisponibles(posicion)"),
                r("lab", "fantasma", "direcciones transitables"),
                f("alt", ("lista vacía", [r("fantasma", "partida", "null")]),
                  ("hay salidas", [m("fantasma", "azar", "Random()"),
                    m("fantasma", "azar", "nextInt(direcciones.size())"),
                    r("azar", "fantasma", "índice aleatorio"),
                    r("fantasma", "partida", "direcciones.get(indice)")]))])),
            n("Se detectan colisiones, se mueve y se resuelven contactos. Un asustado puede ser comido en este mismo tick (R6)."),
            f("loop", ("fin del tick del superpunto: cada fantasma", [
                m("partida", "fantasma", "actualizarEstado(0)"),
                n("tiempoADescontar = sustoRenovado ? 0 : tiempoTranscurrido. Conserva los 10 segundos si sigue ASUSTADO.")])),
            f("loop", ("ticks siguientes sin nuevo superpunto, sin muerte y mientras siga ASUSTADO", [
                m("partida", "fantasma", "actualizarEstado(tiempoTranscurrido)"),
                n("Resta tiempoTranscurrido; con tiempoSustoRestante <= 1e-9, fija 0 y pasa a PERSIGUIENDO.")]))]
    },
    {
        "id": "R06", "name": "Consumo de fantasmas", "participants": [
            ("partida", "Partida"), ("fantasma", "Fantasma"), ("lab", "Laberinto")],
        "sources": ["Partida.actualizar", "Partida.resolverColision", "Fantasma.serComido",
                    "Fantasma.elegirDireccion", "Fantasma.mover", "Fantasma.actualizarEstado"],
        "steps": [
            n("Precondición: detectarColision() encontró un fantasma ASUSTADO. Partida ya movió a los personajes y recorre los contactos (R7)."),
            m("partida", "fantasma", "getEstado()"), r("fantasma", "partida", "ASUSTADO"),
            m("partida", "partida", "resolverColision(fantasma)"),
            m("partida", "fantasma", "getEstado()"),
            n("Partida suma 200 puntos por este fantasma."),
            m("partida", "fantasma", "serComido()"),
            n("Si está ASUSTADO: estado = REGRESANDO; tiempoSustoRestante = 0. En otro estado serComido() retorna sin cambios."),
            n("Este proceso se repite por cada asustado tocado. Si además hay un perseguidor, se cobran todos antes de perder la vida (R7)."),
            f("opt", ("no hubo contacto peligroso", [
                m("partida", "fantasma", "actualizarEstado(tiempoADescontar)"),
                n("Puede volver a PERSIGUIENDO ya en este tick si fue comido sobre su posición inicial.")])),
            f("loop", ("ticks siguientes: continúa REGRESANDO y la partida sigue en curso", [
                m("partida", "fantasma", "elegirDireccion(laberinto, proximaPosicionPacMan)"),
                m("fantasma", "lab", "caminoMasCorto(posicion, posicionInicial)"),
                r("lab", "fantasma", "camino al origen propio"),
                r("fantasma", "partida", "dirección al próximo paso, o null"),
                n("Partida calcula posiciones y detecta contactos antes de mover (R4/R7)."),
                m("partida", "fantasma", "mover(direccion, laberinto)"),
                n("Al resolver contactos, REGRESANDO no daña a PacMan ni vuelve a dar 200 puntos."),
                m("partida", "fantasma", "actualizarEstado(tiempoADescontar)"),
                f("alt", ("posicion == posicionInicial por fila y columna", [
                    n("tiempoSustoRestante = 0; estado = PERSIGUIENDO. Perseguirá a PacMan en el siguiente tick.")]),
                  ("todavía no llegó", [n("Conserva REGRESANDO. Si no hay camino, queda quieto.")]))]))]
    },
    {
        "id": "R07", "name": "Muerte de PacMan", "participants": [
            ("partida", "Partida"), ("pac", "PacMan"), ("fantasma", "Fantasma")],
        "sources": ["Partida.actualizar", "Partida.detectarColision", "Partida.resolverColision",
                    "Partida.perderVida", "PacMan.volverAlInicio", "Fantasma.volverAlInicio"],
        "steps": [
            n("Precondición: puntos consumidos y puntaje sumado; no hubo victoria. Ya están calculadas las próximas posiciones de todos, sin mover todavía."),
            m("partida", "partida", "detectarColision(proximaPosicionPacMan, proximasPosicionesFantasmas)"),
            m("partida", "pac", "getPosicion()"), r("pac", "partida", "posicionPacman actual"),
            f("loop", ("cada uno de los cuatro fantasmas", [
                m("partida", "fantasma", "getPosicion()"),
                r("fantasma", "partida", "actualFantasma"),
                n("mismaLlegada: próxima PacMan == próxima fantasma. intercambio: próxima PacMan == actual fantasma y próxima fantasma == actual PacMan."),
                f("opt", ("mismaLlegada || intercambio", [n("Añadir fantasma a fantasmasTocados.")]))])),
            r("partida", "partida", "fantasmasTocados"),
            m("partida", "pac", "mover(direccionPacMan, laberinto)"),
            f("loop", ("cada fantasma", [m("partida", "fantasma", "mover(direccionesFantasmas[i], laberinto)")])),
            f("loop", ("cada fantasma tocado; contactoPeligroso inicia en false", [
                m("partida", "fantasma", "getEstado()"),
                f("alt", ("ASUSTADO", [m("partida", "partida", "resolverColision(fantasma)"),
                    n("Suma 200 y llama serComido() (R6).")]),
                  ("PERSIGUIENDO", [n("contactoPeligroso = true; todavía no se descuenta vida.")]),
                  ("REGRESANDO", [n("No suma puntos ni marca daño.")]))])),
            f("opt", ("contactoPeligroso == true", [
                m("partida", "partida", "perderVida()"),
                n("Si EN_CURSO y vidas > 0: vidas--. Una sola pérdida aunque toque varios perseguidores."),
                m("partida", "pac", "volverAlInicio()"),
                n("PacMan copia su posición inicial y fija direccion = DERECHA."),
                f("loop", ("cada fantasma", [m("partida", "fantasma", "volverAlInicio()"),
                    n("Copia su origen propio; estado = PERSIGUIENDO; tiempoSustoRestante = 0.")])),
                n("Si vidas == 0: DERROTA (R8). actualizar() retorna antes de actualizar temporizadores. Conserva puntaje y puntos retirados.")]))]
    },
    {
        "id": "R08", "name": "Vidas y derrota", "participants": [
            ("control", "ControladorJuego"), ("partida", "Partida"), ("lab", "Laberinto"),
            ("pac", "PacMan"), ("fantasma", "Fantasma")],
        "sources": ["ControladorJuego.ControladorJuego", "Partida.Partida", "Partida.perderVida",
                    "ControladorJuego.actualizar", "Partida.actualizar"],
        "steps": [
            n("Inicio desde new ControladorJuego(). En la línea de vida fantasma se representa cada instancia creada, no un fantasma único compartido."),
            m("control", "partida", "Partida()"),
            m("partida", "lab", "crearDefault()"),
            m("lab", "lab", "Laberinto()"),
            n("Laberinto() delega a Laberinto(MAPA_POR_DEFECTO), que crea celdas y puntos."),
            r("lab", "partida", "laberinto"),
            m("partida", "partida", "Partida(laberinto, INICIO_PACMAN)"),
            n("vidas = VIDAS_INICIALES = 3; puntaje = 0; estado = EN_CURSO."),
            m("partida", "pac", "PacMan(inicioPacMan)"),
            m("partida", "lab", "centro()"), r("lab", "partida", "centro"),
            f("loop", ("i = 0 .. 3", [m("partida", "fantasma", "Fantasma(posicion)"),
                n("Código actual: misma fila centro.fila; columnas centro.columna + {-2, -1, 1, 2}. Cada fantasma conserva su propio origen.")])),
            r("partida", "control", "partida inicializada"),
            f("loop", ("tres ticks con contacto peligroso, sin victoria; detalles en R7", [
                m("control", "partida", "actualizar(tiempoTranscurrido)"),
                n("Se omiten aquí preparación, consumo, movimiento y detección de colisiones (R1-R7)."),
                m("partida", "partida", "perderVida()"),
                n("Con estado EN_CURSO y vidas > 0: vidas-- (3 → 2 → 1 → 0)."),
                m("partida", "pac", "volverAlInicio()"),
                f("loop", ("cada fantasma", [m("partida", "fantasma", "volverAlInicio()")])),
                f("opt", ("vidas == 0", [n("estado = DERROTA; actualizar() retorna.")]))])),
            m("control", "control", "actualizar(tiempoTranscurrido)"),
            m("control", "partida", "getEstado()"), r("partida", "control", "DERROTA"),
            n("Controlador retorna sin actualizar modelo, ticks ni tiempo. Partida.actualizar() y perderVida() también tienen guardas propias.")]
    },
    {
        "id": "R09", "name": "Victoria", "participants": [
            ("control", "ControladorJuego"), ("partida", "Partida"), ("lab", "Laberinto"),
            ("celda", "Celda"), ("pac", "PacMan")],
        "sources": ["ControladorJuego.actualizar", "Partida.actualizar", "Partida.comprobarVictoria",
                    "Laberinto.quedanPuntos", "Celda.tienePunto"],
        "steps": [
            m("control", "partida", "actualizar(tiempoTranscurrido)"),
            n("Partida retorna de inmediato si estado != EN_CURSO. Esta secuencia comienza EN_CURSO."),
            m("partida", "partida", "comprobarVictoria()"),
            m("partida", "lab", "quedanPuntos()"),
            f("loop", ("cada celda, hasta encontrar un punto", [
                m("lab", "celda", "tienePunto()"), r("celda", "lab", "punto != null")])),
            r("lab", "partida", "hayPuntos"),
            f("alt", ("!hayPuntos al comenzar", [
                n("estado = VICTORIA y return: no mueve ni resuelve contactos.")]),
              ("todavía quedan puntos", [
                m("partida", "pac", "getDireccion()"),
                m("partida", "pac", "calcularProximaPosicion(direccionPacMan, laberinto)"),
                m("partida", "lab", "obtenerCelda(proximaPosicionPacMan)"),
                f("opt", ("celdaDestino != null", [m("partida", "celda", "retirarPunto()"),
                    m("partida", "partida", "consumir(puntoConsumido)"),
                    n("Suma 10 o 50; aplica susto si corresponde (R3/R5). Incluye puntos y superpuntos.")])),
                m("partida", "partida", "comprobarVictoria()"),
                m("partida", "lab", "quedanPuntos()"),
                r("lab", "partida", "hayPuntos después del consumo"),
                f("alt", ("!hayPuntos: era el último punto", [
                    m("partida", "pac", "mover(direccionPacMan, laberinto)"),
                    n("estado = VICTORIA y return ANTES de preparar fantasmas y detectar colisiones. Ganar tiene prioridad sobre morir en ese tick.")]),
                  ("quedan puntos", [n("Continúa con fantasmas, colisiones y estados (R4-R7).")]))])),
            r("partida", "control", "fin del tick"),
            n("Controlador cuenta este último tick. Si ganó, las siguientes llamadas a actualizar() retornan al leer VICTORIA y no incrementan contadores.")]
    },
    {
        "id": "R10", "name": "Control del juego", "participants": [
            ("vista", "JuegoConsola"), ("control", "ControladorJuego"),
            ("partida", "Partida"), ("nueva", "Partida")],
        "sources": ["JuegoConsola.procesar", "ControladorJuego.alternarPausa", "ControladorJuego.pausar",
                    "ControladorJuego.reanudar", "ControladorJuego.avanzarTurno",
                    "ControladorJuego.nuevaPartida", "Partida.pausar", "Partida.reanudar"],
        "steps": [
            n("Dos acciones de consola independientes: P alterna pausa; N intenta iniciar otra partida. partida es la actual; nueva es otra instancia."),
            m("vista", "vista", 'procesar("p")'),
            m("vista", "control", "alternarPausa()"),
            m("control", "partida", "getEstado()"),
            f("alt", ("estadoActual == PAUSADA", [m("control", "control", "reanudar()"),
                m("control", "partida", "reanudar()"), n("Si PAUSADA: estado = EN_CURSO.")]),
              ("otro estado", [m("control", "control", "pausar()"),
                m("control", "partida", "pausar()"),
                n("Solo EN_CURSO cambia a PAUSADA; VICTORIA y DERROTA no cambian.")])),
            f("opt", ("se intenta avanzar mientras está PAUSADA", [
                m("vista", "control", "avanzarTurno(direccion)"),
                m("control", "partida", "getEstado()"), r("partida", "control", "PAUSADA"),
                n("Retorna antes de elegir dirección o actualizar. Enter solo pasa por actualizar(), que tiene la misma guarda; congela movimiento, susto, ticks y tiempo.")])),
            n("Otra pulsación P sobre PAUSADA recorre la rama reanudar() y permite seguir jugando."),
            m("vista", "vista", 'procesar("n")'),
            m("vista", "control", "nuevaPartida()"),
            m("control", "partida", "getEstado()"),
            f("alt", ("VICTORIA o DERROTA", [m("control", "nueva", "Partida()"),
                n("Se construye un modelo completo nuevo (R8): mapa y puntos nuevos, 3 vidas, puntaje 0, personajes iniciales y EN_CURSO."),
                n("Controlador reemplaza partida por la nueva instancia; ticks = 0; tiempoSimulado = 0."),
                r("control", "vista", "true"), n('JuegoConsola muestra "Partida nueva."')]),
              ("EN_CURSO o PAUSADA", [r("control", "vista", "false"),
                n("Conserva la instancia y los contadores. La consola informa que debe ganar o perder antes de reiniciar.")]))]
    }
]
