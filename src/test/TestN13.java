package test;

import controlador.ControladorJuego;
import extra.EstadoFantasma;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.Partida;
import modelo.PuntoComun;

/**
 * Pruebas de N13 preparadas con asistencia de IA.
 * Ejecutar main; no requiere JUnit ni -ea.
 * Desde TPO-Pacman, despues de compilar: java -cp bin test.TestN13
 * Reflection solo permite obtener los fantasmas reales de la partida.
 * Se invoca resolverColision directamente: la deteccion automatica es de N15.
 * Las consultas de puntaje, vidas y estados pasan por ControladorJuego.
 * La perdida de vida ante PERSIGUIENDO corresponde a N14 y no se exige aqui.
 */
public class TestN13 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Comer suma 200 sobre el puntaje anterior y conserva vidas", TestN13::comerAsustado);
        ejecutar("Repetir el contacto no vuelve a premiar", TestN13::contactoRepetido);
        ejecutar("Un fantasma que ya regresaba no da premio", TestN13::yaRegresando);
        ejecutar("Cada uno de los cuatro fantasmas da 200", TestN13::variosFantasmas);
        ejecutar("El mismo fantasma puede dar premio con un susto nuevo", TestN13::nuevoSusto);
        ejecutar("Un perseguidor no da premio", TestN13::perseguidorSinPremio);
        System.out.println("\nN13: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N13 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static Partida nuevaPartida() {
        Laberinto mapa = new Laberinto(new String[] {
            "#######", "#.....#", "#.....#", "#.....#", "#######"
        });
        // PacMan y los fantasmas coinciden en el centro: el contacto ya existe.
        return new Partida(mapa, new Posicion(2, 3));
    }

    private static void comerAsustado() {
        for (boolean conPuntajePrevio : new boolean[] {false, true}) {
            Partida partida = nuevaPartida();
            ControladorJuego controlador = new ControladorJuego(partida);
            if (conPuntajePrevio) {
                partida.consumir(new PuntoComun());
            }
            int anterior = controlador.getPuntaje();
            int vidas = controlador.getVidas();
            Fantasma fantasma = fantasmas(partida)[0];
            fantasma.asustar(10);

            partida.resolverColision(fantasma);

            comprobar("Tras comer (puntaje previo " + anterior + ")", controlador,
                anterior + 200, vidas, 0, EstadoFantasma.REGRESANDO);
            for (int i = 1; i < 4; i++) {
                verificar("El otro fantasma " + i + " conserva su estado",
                    EstadoFantasma.PERSIGUIENDO, controlador.getEstadosFantasmas()[i]);
            }
        }
    }

    private static void contactoRepetido() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        int vidas = controlador.getVidas();
        Fantasma fantasma = fantasmas(partida)[0];
        fantasma.asustar(10);
        partida.resolverColision(fantasma);
        comprobar("Primer contacto", controlador, 200, vidas, 0, EstadoFantasma.REGRESANDO);

        for (int contacto = 2; contacto <= 4; contacto++) {
            partida.resolverColision(fantasma);
            comprobar("Contacto " + contacto, controlador, 200, vidas,
                0, EstadoFantasma.REGRESANDO);
        }
    }

    private static void yaRegresando() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        int vidas = controlador.getVidas();
        Fantasma fantasma = fantasmas(partida)[1];
        fantasma.asustar(10);
        fantasma.serComido();
        verificar("Preparacion: ya esta regresando", EstadoFantasma.REGRESANDO, fantasma.getEstado());

        partida.resolverColision(fantasma);

        comprobar("Contacto durante un regreso previo", controlador, 0, vidas,
            1, EstadoFantasma.REGRESANDO);
    }

    private static void variosFantasmas() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        int vidas = controlador.getVidas();
        Fantasma[] fantasmas = fantasmas(partida);
        for (Fantasma fantasma : fantasmas) {
            fantasma.asustar(10);
        }
        for (int i = 0; i < fantasmas.length; i++) {
            partida.resolverColision(fantasmas[i]);
            comprobar("Comer fantasma " + i, controlador, (i + 1) * 200, vidas,
                i, EstadoFantasma.REGRESANDO);
        }
        for (int i = 0; i < fantasmas.length; i++) {
            partida.resolverColision(fantasmas[i]);
            comprobar("Repetir contacto con fantasma " + i, controlador, 800, vidas,
                i, EstadoFantasma.REGRESANDO);
        }
    }

    private static void nuevoSusto() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        int vidas = controlador.getVidas();
        Fantasma fantasma = fantasmas(partida)[0];
        fantasma.asustar(10);
        partida.resolverColision(fantasma);
        comprobar("Primera vez comido", controlador, 200, vidas, 0, EstadoFantasma.REGRESANDO);

        // Ya esta en su origen; termina el regreso sin mover personajes.
        fantasma.actualizarEstado(0);
        verificar("Finalizo el regreso", EstadoFantasma.PERSIGUIENDO, fantasma.getEstado());
        fantasma.asustar(10);
        partida.resolverColision(fantasma);

        comprobar("Comido tras un nuevo susto", controlador, 400, vidas, 0, EstadoFantasma.REGRESANDO);
        partida.resolverColision(fantasma);
        comprobar("No repite el segundo premio", controlador, 400, vidas, 0, EstadoFantasma.REGRESANDO);
    }

    private static void perseguidorSinPremio() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        partida.consumir(new PuntoComun());
        int anterior = controlador.getPuntaje();

        partida.resolverColision(fantasmas(partida)[0]);

        verificar("PERSIGUIENDO no suma puntos", anterior, controlador.getPuntaje());
    }

    private static Fantasma[] fantasmas(Partida partida) {
        try {
            Field campo = Partida.class.getDeclaredField("fantasmas");
            campo.setAccessible(true);
            return (Fantasma[]) campo.get(partida);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudieron consultar los fantasmas", error);
        }
    }

    private static void comprobar(String caso, ControladorJuego controlador,
            int puntaje, int vidas, int indice, EstadoFantasma estado) {
        verificar(caso + ": puntaje", puntaje, controlador.getPuntaje());
        verificar(caso + ": vidas", vidas, controlador.getVidas());
        verificar(caso + ": estado", estado, controlador.getEstadosFantasmas()[indice]);
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
