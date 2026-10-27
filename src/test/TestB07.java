package test;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.Partida;

/**
 * Pruebas B07 (R4), preparadas con asistencia de IA.
 * Ejecutar main: no requiere JUnit ni la opcion -ea.
 * Las coordenadas esperadas se calcularon a mano, sin usar caminoMasCorto().
 */
public class TestB07 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Inicio de los cuatro fantasmas", TestB07::inicio);
        ejecutar("Movimiento de los cuatro mediante el controlador", TestB07::integracion);
        ejecutar("Cuatro direcciones", TestB07::direcciones);
        ejecutar("Pasillo con curvas", TestB07::pasillo);
        ejecutar("Paredes y bordes", TestB07::obstaculos);
        ejecutar("Cruce con rutas de diferente longitud", TestB07::cruce);
        ejecutar("Salida de callejon", TestB07::callejon);
        ejecutar("Empates entre caminos minimos", TestB07::empates);
        ejecutar("Sin camino y objetivo alcanzado", TestB07::sinMovimiento);
        ejecutar("No avanzar fuera de EN_CURSO", TestB07::partidaDetenida);
        System.out.println("\nB07: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("B07 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static void inicio() {
        Partida partida = new Partida();
        Fantasma[] fantasmas = leerFantasmas(partida);
        verificar("Partida en curso", EstadoPartida.EN_CURSO, partida.getEstado());
        verificar("Cantidad de fantasmas", 4, fantasmas.length);
        for (int i = 0; i < fantasmas.length; i++) {
            posicion("Fantasma " + i + " en el centro", 7, 7, fantasmas[i]);
            verificar("Fantasma " + i + " persigue", EstadoFantasma.PERSIGUIENDO, fantasmas[i].getEstado());
            for (int j = 0; j < i; j++) {
                verificar("Objetos distintos: " + i + " y " + j, true, fantasmas[i] != fantasmas[j]);
            }
        }
    }

    private static Laberinto mapaIntegracion() {
        // Centro (2,3). Pac-Man sube de (2,5) a (1,5).
        return mapa("#######", "#####.#", "#.....#", "#######", "#######");
    }

    private static void integracion() {
        Partida partida = new Partida(mapaIntegracion(), new Posicion(2, 5));
        ControladorJuego controlador = new ControladorJuego(partida);
        Fantasma[] fantasmas = leerFantasmas(partida);
        verificar("Cuatro fantasmas en el mapa pequeno", 4, fantasmas.length);
        for (int i = 0; i < fantasmas.length; i++) {
            posicion("Centro del mapa pequeno: fantasma " + i, 2, 3, fantasmas[i]);
        }
        controlador.moverPacMan(Direccion.ARRIBA);
        controlador.actualizar(0.25);
        posicion("Pac-Man subio", 1, 5, controlador.getPosicionPacMan());
        for (int i = 0; i < fantasmas.length; i++) {
            posicion("Tick 1: fantasma " + i, 2, 4, fantasmas[i]);
        }
        controlador.actualizar(2.0);
        for (int i = 0; i < fantasmas.length; i++) {
            posicion("Tick 2: fantasma " + i + " avanza solo una celda", 2, 5, fantasmas[i]);
            verificar("Sigue persiguiendo: " + i, EstadoFantasma.PERSIGUIENDO, fantasmas[i].getEstado());
        }
        // Cambiar la posicion de Pac-Man debe cambiar la decision del fantasma.
        // Antes de mover Pac-Man (2,1), iria a la izquierda desde (2,2).
        // Despues de subir Pac-Man a (1,1), el empate se resuelve hacia arriba.
        Partida cambioObjetivo = new Partida(sala(), new Posicion(2, 1));
        ControladorJuego otroControlador = new ControladorJuego(cambioObjetivo);
        otroControlador.moverPacMan(Direccion.ARRIBA);
        otroControlador.actualizar(1);
        posicion("Pac-Man en su nueva posicion", 1, 1, otroControlador.getPosicionPacMan());
        Fantasma[] otrosFantasmas = leerFantasmas(cambioObjetivo);
        verificar("Cuatro fantasmas al cambiar el objetivo", 4, otrosFantasmas.length);
        for (int i = 0; i < otrosFantasmas.length; i++) {
            posicion("Fantasma " + i + " persigue la posicion actual", 1, 2, otrosFantasmas[i]);
        }
        // Nos detenemos antes del contacto: sus consecuencias son otra tarjeta.
    }

    private static Laberinto sala() {
        return mapa("#####", "#...#", "#...#", "#...#", "#####");
    }

    private static void direcciones() {
        int[][] destinos = {{1, 2}, {3, 2}, {2, 1}, {2, 3}};
        Direccion[] esperadas = {Direccion.ARRIBA, Direccion.ABAJO, Direccion.IZQUIERDA, Direccion.DERECHA};
        Laberinto mapa = sala();
        for (int i = 0; i < destinos.length; i++) {
            Fantasma fantasma = new Fantasma(new Posicion(2, 2));
            Posicion destino = new Posicion(destinos[i][0], destinos[i][1]);
            Direccion elegida = fantasma.elegirDireccion(mapa, destino);
            verificar("Elegir " + esperadas[i], esperadas[i], elegida);
            posicion("Elegir no mueve al fantasma", 2, 2, fantasma);
            fantasma.mover(elegida, mapa);
            posicion("Mover " + esperadas[i], destino.fila, destino.columna, fantasma);
        }
    }

    private static void pasillo() {
        Laberinto mapa = mapa("#######", "#...###", "###.###", "###...#", "#######");
        int[][] recorrido = {{1, 2}, {1, 3}, {2, 3}, {3, 3}, {3, 4}, {3, 5}};
        recorrer(new Fantasma(new Posicion(1, 1)), mapa, new Posicion(3, 5), recorrido);
    }

    private static void obstaculos() {
        Laberinto cerrado = mapa("###", "#.#", "###");
        Fantasma fantasma = new Fantasma(new Posicion(1, 1));
        for (Direccion direccion : Direccion.values()) {
            fantasma.mover(direccion, cerrado);
            posicion("Pared hacia " + direccion, 1, 1, fantasma);
        }
        Laberinto abierto = mapa("..", "..");
        fantasma = new Fantasma(new Posicion(0, 0));
        fantasma.mover(Direccion.ARRIBA, abierto);
        posicion("Borde superior", 0, 0, fantasma);
        fantasma.mover(Direccion.IZQUIERDA, abierto);
        posicion("Borde izquierdo", 0, 0, fantasma);
        fantasma = new Fantasma(new Posicion(1, 1));
        fantasma.mover(Direccion.ABAJO, abierto);
        posicion("Borde inferior", 1, 1, fantasma);
        fantasma.mover(Direccion.DERECHA, abierto);
        posicion("Borde derecho", 1, 1, fantasma);
    }

    private static void cruce() {
        Laberinto mapa = mapa("#######", "#.....#", "#.#.#.#", "#...#.#",
            "###.#.#", "#.....#", "#######");
        // Desde (3,3) a (1,5): arriba lleva en 4 pasos.
        // Rodear por izquierda o abajo sin volver al cruce requiere 8 pasos.
        Fantasma fantasma = new Fantasma(new Posicion(3, 3));
        verificar("Elige el camino corto", Direccion.ARRIBA,
            fantasma.elegirDireccion(mapa, new Posicion(1, 5)));
        int[][] recorrido = {{2, 3}, {1, 3}, {1, 4}, {1, 5}};
        recorrer(fantasma, mapa, new Posicion(1, 5), recorrido);
    }

    private static void callejon() {
        Laberinto mapa = mapa("#####", "#...#", "#####");
        Fantasma fantasma = new Fantasma(new Posicion(1, 2));
        fantasma.mover(Direccion.IZQUIERDA, mapa);
        posicion("Entro al extremo cerrado", 1, 1, fantasma);
        Direccion salida = fantasma.elegirDireccion(mapa, new Posicion(1, 3));
        verificar("Invierte el movimiento para salir", Direccion.DERECHA, salida);
        fantasma.mover(salida, mapa);
        posicion("Salio del callejon", 1, 2, fantasma);
    }

    private static void empates() {
        // Cada esquina tiene dos rutas minimas de dos pasos desde (2,2).
        empate(sala(), 2, 2, 1, 1, Direccion.ARRIBA);
        empate(sala(), 2, 2, 1, 3, Direccion.ARRIBA);
        empate(sala(), 2, 2, 3, 1, Direccion.ABAJO);
        empate(sala(), 2, 2, 3, 3, Direccion.ABAJO);
        Laberinto rodeo = mapa("#######", "#.....#", "#..#..#", "#..#..#", "#.....#", "#######");
        // Dos rodeos de cinco pasos. Izquierda gana frente a derecha.
        empate(rodeo, 4, 3, 1, 3, Direccion.IZQUIERDA);
    }

    private static void empate(Laberinto mapa, int fila, int columna,
            int destinoFila, int destinoColumna, Direccion esperada) {
        Fantasma fantasma = new Fantasma(new Posicion(fila, columna));
        for (int intento = 1; intento <= 3; intento++) {
            verificar("Empate hacia (" + destinoFila + "," + destinoColumna + "), intento " + intento,
                esperada, fantasma.elegirDireccion(mapa, new Posicion(destinoFila, destinoColumna)));
        }
    }

    private static void sinMovimiento() {
        Laberinto separado = mapa("#####", "#.#.#", "#####");
        Fantasma fantasma = new Fantasma(new Posicion(1, 1));
        Direccion direccion = fantasma.elegirDireccion(separado, new Posicion(1, 3));
        verificar("Destino inaccesible devuelve null", null, direccion);
        fantasma.mover(direccion, separado);
        posicion("Sin camino permanece quieto", 1, 1, fantasma);
        direccion = fantasma.elegirDireccion(separado, new Posicion(1, 1));
        verificar("En el objetivo devuelve null", null, direccion);
        fantasma.mover(direccion, separado);
        posicion("En el objetivo permanece quieto", 1, 1, fantasma);
    }

    private static void partidaDetenida() {
        for (EstadoPartida estado : new EstadoPartida[] {
                EstadoPartida.PAUSADA, EstadoPartida.VICTORIA, EstadoPartida.DERROTA}) {
            Partida partida = new Partida(mapaIntegracion(), new Posicion(2, 5));
            fijarEstadoParaPrueba(partida, estado);
            new ControladorJuego(partida).actualizar(1);
            Fantasma[] fantasmas = leerFantasmas(partida);
            verificar("Conserva cuatro fantasmas en " + estado, 4, fantasmas.length);
            for (int i = 0; i < fantasmas.length; i++) {
                posicion(estado + ": fantasma " + i + " no avanza", 2, 3, fantasmas[i]);
            }
        }
    }

    private static Laberinto mapa(String... filas) {
        return new Laberinto(filas);
    }

    private static void recorrer(Fantasma fantasma, Laberinto mapa, Posicion destino, int[][] esperado) {
        for (int i = 0; i < esperado.length; i++) {
            Posicion anterior = fantasma.getPosicion();
            Direccion direccion = fantasma.elegirDireccion(mapa, destino);
            fantasma.mover(direccion, mapa);
            Posicion actual = fantasma.getPosicion();
            posicion("Paso " + (i + 1), esperado[i][0], esperado[i][1], actual);
            int distancia = Math.abs(actual.fila - anterior.fila) + Math.abs(actual.columna - anterior.columna);
            verificar("Avanzo exactamente una celda", 1, distancia);
            verificar("No entro en una pared", true, mapa.esTransitable(actual));
        }
    }

    private static void ejecutar(String nombre, Runnable caso) {
        System.out.println("\n-- " + nombre + " --");
        try {
            caso.run();
        } catch (RuntimeException | AssertionError error) {
            pruebas++;
            System.out.println("[FALLA] Excepcion en el caso: " + error);
        }
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        pruebas++;
        boolean ok = Objects.equals(esperado, obtenido);
        if (ok) {
            aprobadas++;
        }
        System.out.println((ok ? "[OK] " : "[FALLA] ") + descripcion
            + " | esperado: " + esperado + " | obtenido: " + obtenido);
    }

    private static void posicion(String descripcion, int fila, int columna, Fantasma fantasma) {
        posicion(descripcion, fila, columna, fantasma.getPosicion());
    }

    private static void posicion(String descripcion, int fila, int columna, Posicion obtenida) {
        String esperado = "(" + fila + "," + columna + ")";
        String actual = obtenida == null ? "null" : "(" + obtenida.fila + "," + obtenida.columna + ")";
        verificar(descripcion, esperado, actual);
    }

    // Auxiliares de prueba: no existe consulta publica de los fantasmas.
    // Reflection permite inspeccionar el campo privado sin cambiar el negocio.
    // Si se cambia su nombre, esta prueba falla con un mensaje explicito.
    private static Fantasma[] leerFantasmas(Partida partida) {
        try {
            Field campo = Partida.class.getDeclaredField("fantasmas");
            campo.setAccessible(true);
            return ((Fantasma[]) campo.get(partida)).clone();
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo inspeccionar Partida.fantasmas", error);
        }
    }

    // Prepara el estado para probar la guarda de actualizar(). No comprueba
    // pausar(), reanudar(), victoria ni derrota: pertenecen a otras tareas.
    private static void fijarEstadoParaPrueba(Partida partida, EstadoPartida estado) {
        try {
            Field campo = Partida.class.getDeclaredField("estado");
            campo.setAccessible(true);
            campo.set(partida, estado);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo preparar Partida.estado", error);
        }
    }
}
