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
 * Pruebas de N10 preparadas con asistencia de IA.
 * Ejecutar main; no requiere JUnit ni -ea.
 * El avance se ejecuta mediante ControladorJuego y se consulta por su API.
 * Reflection solo prepara fantasmas/estados sin agregar setters al negocio.
 * El descuento del susto y las prioridades de contactos corresponden a N15.
 */
public class TestN10 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Los cuatro persiguen; el tiempo no multiplica pasos", TestN10::persecucion);
        ejecutar("Estados mezclados y llegada al origen", TestN10::estadosMezclados);
        ejecutar("Asustados respetan paredes y limites", TestN10::asustados);
        ejecutar("Sin camino ni salidas permanecen quietos", TestN10::sinSalida);
        ejecutar("Una decision y un movimiento por fantasma y paso", TestN10::decisionUnica);
        ejecutar("No avanzar fuera de EN_CURSO", TestN10::partidaDetenida);
        ejecutar("No mover tras consumir el ultimo punto", TestN10::victoria);
        System.out.println("\nN10: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N10 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static Laberinto sala() {
        return new Laberinto(new String[] {
            "#########", "#.......#", "#.......#", "#.......#", "#########"
        });
    }

    private static void persecucion() {
        for (double segundos : new double[] {0, 0.25, 5, 1000}) {
            Laberinto mapa = sala();
            Partida partida = new Partida(mapa, new Posicion(2, 7));
            ControladorJuego controlador = new ControladorJuego(partida);
            controlador.moverPacMan(Direccion.ARRIBA);
            Posicion[] anteriores = controlador.getPosicionesFantasmas();
            verificar("Hay cuatro fantasmas", 4, anteriores.length);
            controlador.actualizar(segundos);
            posicion("PacMan se movio antes de elegir el objetivo", 1, 7,
                controlador.getPosicionPacMan());
            Posicion[] actuales = controlador.getPosicionesFantasmas();
            for (int i = 0; i < 4; i++) {
                // Desde (2,4) hacia (1,7), subir o ir a derecha son pasos minimos.
                // No se exige un desempate concreto entre ambos.
                verificar("Fantasma " + i + " elige un paso minimo con tiempo " + segundos,
                    true, es(actuales[i], 1, 4) || es(actuales[i], 2, 5));
                pasoValido("Persecucion " + i, mapa, anteriores[i], actuales[i], true);
                verificar("Sigue persiguiendo", EstadoFantasma.PERSIGUIENDO,
                    controlador.getEstadosFantasmas()[i]);
            }
        }
    }

    private static void estadosMezclados() {
        Laberinto mapa = sala();
        Partida partida = new Partida(mapa, new Posicion(2, 7));
        ControladorJuego controlador = new ControladorJuego(partida);
        Fantasma[] fantasmas = fantasmas(partida);
        fantasmas[1].mover(Direccion.ARRIBA, mapa);
        fantasmas[1].asustar(10);
        fantasmas[2].mover(Direccion.IZQUIERDA, mapa);
        fantasmas[2].mover(Direccion.IZQUIERDA, mapa);
        fantasmas[2].asustar(10);
        fantasmas[2].serComido();
        fantasmas[3].mover(Direccion.DERECHA, mapa);
        fantasmas[3].asustar(10);
        fantasmas[3].serComido();

        controlador.moverPacMan(Direccion.ARRIBA);
        Posicion[] anteriores = controlador.getPosicionesFantasmas();
        controlador.actualizar(0);
        Posicion[] actuales = controlador.getPosicionesFantasmas();
        for (int i = 0; i < 4; i++) {
            pasoValido("Estado mezclado " + i, mapa, anteriores[i], actuales[i], true);
        }
        verificar("Perseguidor avanza hacia PacMan", true,
            es(actuales[0], 1, 4) || es(actuales[0], 2, 5));
        verificar("Asustado conserva su estado", EstadoFantasma.ASUSTADO,
            controlador.getEstadosFantasmas()[1]);
        posicion("Regresando avanza al origen y no hacia PacMan", 2, 3, actuales[2]);
        verificar("Aun no llego al origen", EstadoFantasma.REGRESANDO,
            controlador.getEstadosFantasmas()[2]);
        posicion("El otro regresando alcanza el origen", 2, 4, actuales[3]);
        verificar("Recupera persecucion en el mismo paso de llegada",
            EstadoFantasma.PERSIGUIENDO, controlador.getEstadosFantasmas()[3]);

        controlador.actualizar(0);
        posicion("El primero tambien alcanza el origen", 2, 4,
            controlador.getPosicionesFantasmas()[2]);
        verificar("El primero recupera persecucion", EstadoFantasma.PERSIGUIENDO,
            controlador.getEstadosFantasmas()[2]);
        Posicion siguiente = controlador.getPosicionesFantasmas()[3];
        verificar("El que ya llego vuelve a perseguir en el siguiente paso", true,
            es(siguiente, 1, 4) || es(siguiente, 2, 5));
    }

    private static void asustados() {
        Laberinto[] mapas = {new Laberinto(new String[] {
            "#########",
            "# #######",
            "###...###",
            "#########",
            "#########"
        })};
        Posicion[] inicios = {new Posicion(1, 1)};
        for (int m = 0; m < mapas.length; m++) {
            Partida partida = new Partida(mapas[m], inicios[m]);
            ControladorJuego controlador = new ControladorJuego(partida);
            // PacMan queda separado del pasillo de fantasmas y no puede provocar
            // una colision o consumir los puntos durante esta prueba de movimiento.
            controlador.moverPacMan(Direccion.IZQUIERDA);
            for (Fantasma fantasma : fantasmas(partida)) {
                fantasma.asustar(10);
            }
            for (int tick = 0; tick < 20; tick++) {
                Posicion[] anteriores = controlador.getPosicionesFantasmas();
                controlador.actualizar(0);
                Posicion[] actuales = controlador.getPosicionesFantasmas();
                for (int i = 0; i < 4; i++) {
                    // Se comprueba validez de cada resultado, no una secuencia aleatoria.
                    pasoValido("Asustado " + i + ", mapa " + m + ", paso " + tick,
                        mapas[m], anteriores[i], actuales[i], true);
                }
            }
        }
    }

    private static void sinSalida() {
        Laberinto mapa = new Laberinto(new String[] {
            "#######", "#..####", "### ###", "#######", "#######"
        });
        Partida partida = new Partida(mapa, new Posicion(1, 1));
        ControladorJuego controlador = new ControladorJuego(partida);
        controlador.moverPacMan(Direccion.ARRIBA);
        Fantasma[] fantasmas = fantasmas(partida);
        fantasmas[1].asustar(10);
        fantasmas[2].asustar(10);
        fantasmas[2].serComido(); // Ya esta en el origen; no debe dar un paso extra.
        controlador.actualizar(0);
        verificar("Quedan puntos y la partida sigue en curso", EstadoPartida.EN_CURSO,
            controlador.getEstado());
        for (Posicion posicion : controlador.getPosicionesFantasmas()) {
            posicion("Sin salida permanece quieto", 2, 3, posicion);
        }
        verificar("Regresando en origen recupera persecucion", EstadoFantasma.PERSIGUIENDO,
            controlador.getEstadosFantasmas()[2]);
    }

    private static void decisionUnica() {
        Partida partida = new Partida(sala(), new Posicion(1, 1));
        ControladorJuego controlador = new ControladorJuego(partida);
        controlador.moverPacMan(Direccion.ARRIBA);
        FantasmaContador[] contadores = instalarContadores(partida);
        for (int tick = 1; tick <= 3; tick++) {
            controlador.actualizar(500);
            for (int i = 0; i < contadores.length; i++) {
                FantasmaContador fantasma = contadores[i];
                verificar("Una decision por paso: " + i, tick, fantasma.decisiones);
                verificar("Un movimiento por paso: " + i, tick, fantasma.movimientos);
                verificar("Se aplica la misma direccion elegida: " + i,
                    fantasma.elegida, fantasma.aplicada);
                posicion("No se recalcula ni se mueve de mas: " + i, 2,
                    tick % 2 == 1 ? 5 : 4, controlador.getPosicionesFantasmas()[i]);
            }
        }
    }

    private static void partidaDetenida() {
        for (EstadoPartida estado : new EstadoPartida[] {
                EstadoPartida.PAUSADA, EstadoPartida.VICTORIA, EstadoPartida.DERROTA}) {
            Partida partida = new Partida(sala(), new Posicion(1, 1));
            FantasmaContador[] contadores = instalarContadores(partida);
            campo(partida, "estado", estado);
            ControladorJuego controlador = new ControladorJuego(partida);
            controlador.actualizar(20);
            verificar("Conserva estado " + estado, estado, controlador.getEstado());
            for (FantasmaContador fantasma : contadores) {
                verificar("No decide en " + estado, 0, fantasma.decisiones);
                verificar("No se mueve en " + estado, 0, fantasma.movimientos);
                posicion("Conserva posicion en " + estado, 2, 4, fantasma.getPosicion());
            }
        }
    }

    private static void victoria() {
        Laberinto mapa = new Laberinto(new String[] {
            "#########", "# .     #", "#       #", "#       #", "#########"
        });
        Partida partida = new Partida(mapa, new Posicion(1, 1));
        FantasmaContador[] contadores = instalarContadores(partida);
        ControladorJuego controlador = new ControladorJuego(partida);
        controlador.actualizar(1);
        verificar("Consumir el ultimo punto declara victoria", EstadoPartida.VICTORIA,
            controlador.getEstado());
        for (FantasmaContador fantasma : contadores) {
            verificar("No decide despues de ganar", 0, fantasma.decisiones);
            verificar("No mueve despues de ganar", 0, fantasma.movimientos);
        }
    }

    private static FantasmaContador[] instalarContadores(Partida partida) {
        Fantasma[] fantasmas = fantasmas(partida);
        FantasmaContador[] contadores = new FantasmaContador[fantasmas.length];
        for (int i = 0; i < fantasmas.length; i++) {
            contadores[i] = new FantasmaContador(fantasmas[i].getPosicion());
            contadores[i].asustar(10);
            fantasmas[i] = contadores[i];
        }
        return contadores;
    }

    // Alternar decisiones detecta una segunda eleccion, aunque ambas sean validas.
    // Solo comprueba la coordinacion; el comportamiento real se prueba arriba.
    private static class FantasmaContador extends Fantasma {
        private int decisiones;
        private int movimientos;
        private Direccion elegida;
        private Direccion aplicada;

        FantasmaContador(Posicion inicio) {
            super(inicio);
        }

        @Override
        public Direccion elegirDireccion(Laberinto laberinto, Posicion pacman) {
            decisiones++;
            elegida = decisiones % 2 == 1 ? Direccion.DERECHA : Direccion.IZQUIERDA;
            return elegida;
        }

        @Override
        public void mover(Direccion direccion, Laberinto laberinto) {
            movimientos++;
            aplicada = direccion;
            super.mover(direccion, laberinto);
        }
    }

    private static Fantasma[] fantasmas(Partida partida) {
        try {
            Field campo = Partida.class.getDeclaredField("fantasmas");
            campo.setAccessible(true);
            return (Fantasma[]) campo.get(partida);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudieron preparar los fantasmas", error);
        }
    }

    private static void campo(Partida partida, String nombre, Object valor) {
        try {
            Field campo = Partida.class.getDeclaredField(nombre);
            campo.setAccessible(true);
            campo.set(partida, valor);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo preparar " + nombre, error);
        }
    }

    private static void pasoValido(String caso, Laberinto mapa, Posicion anterior,
            Posicion actual, boolean debeMoverse) {
        int distancia = Math.abs(actual.fila - anterior.fila)
            + Math.abs(actual.columna - anterior.columna);
        verificar(caso + ": distancia valida", true,
            debeMoverse ? distancia == 1 : distancia <= 1);
        verificar(caso + ": dentro del mapa y sin pared", true, mapa.esTransitable(actual));
    }

    private static boolean es(Posicion posicion, int fila, int columna) {
        return posicion.fila == fila && posicion.columna == columna;
    }

    private static void posicion(String caso, int fila, int columna, Posicion actual) {
        verificar(caso, "(" + fila + "," + columna + ")",
            "(" + actual.fila + "," + actual.columna + ")");
    }

    private static void verificar(String caso, Object esperado, Object obtenido) {
        pruebas++;
        boolean ok = Objects.equals(esperado, obtenido);
        if (ok) {
            aprobadas++;
        }
        System.out.println((ok ? "[OK] " : "[FALLA] ") + caso
            + " | esperado: " + esperado + " | obtenido: " + obtenido);
    }

    private static void ejecutar(String nombre, Runnable caso) {
        System.out.println("\n-- " + nombre + " --");
        try {
            caso.run();
        } catch (RuntimeException | AssertionError error) {
            pruebas++;
            System.out.println("[FALLA] " + nombre + ": " + error);
        }
    }
}
