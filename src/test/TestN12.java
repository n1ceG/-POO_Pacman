package test;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.PacMan;
import modelo.Partida;
import modelo.Punto;
import modelo.SuperPunto;

public class TestN12 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Descuenta una vida y reinicia PacMan y fantasmas", TestN12::descontarYReiniciar);
        ejecutar("Conserva puntaje y puntos ya consumidos", TestN12::conservarPuntaje);
        ejecutar("Al quedar vidas, la partida continua", TestN12::continuarConVidas);
        ejecutar("Al llegar a cero vidas termina en DERROTA y se detiene", TestN12::derrota);
        System.out.println("\nN12: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N12 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static Laberinto mapa() {
        return new Laberinto(new String[] {
            "#######",
            "#.....#",
            "#.....#",
            "#.....#",
            "#######"
        });
    }

    private static void descontarYReiniciar() {
        Laberinto mapa = mapa();
        Posicion inicioPacMan = new Posicion(2, 1);
        Posicion centro = mapa.centro();
        Partida partida = new Partida(mapa, inicioPacMan);
        Fantasma[] fantasmas = fantasmas(partida);

        partida.consumir(new SuperPunto());
        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("PacMan se alejo del inicio",
            false, mismaPosicion(partida.getPosicionPacMan(), inicioPacMan));
        for (int i = 0; i < fantasmas.length; i++) {
            verificar("Fantasma " + i + " se alejo del centro",
                false, mismaPosicion(fantasmas[i].getPosicion(), centro));
        }

        fantasmas[0].actualizarEstado(3);
        fantasmas[1].serComido();
        fantasmas[2].actualizarEstado(10);
        partida.elegirDireccionPacman(Direccion.IZQUIERDA);

        partida.perderVida();

        verificar("Vidas tras una perdida", 2, partida.getVidas());
        verificar("La partida sigue EN_CURSO", EstadoPartida.EN_CURSO, partida.getEstado());
        verificar("PacMan vuelve al inicio", true,
            mismaPosicion(partida.getPosicionPacMan(), inicioPacMan));
        verificar("PacMan reaparece mirando a la derecha",
            Direccion.DERECHA, pacMan(partida).getDireccion());
        comprobarFantasmas("Tras reaparecer", partida, centro);
    }

    private static void conservarPuntaje() {
        Laberinto mapa = mapa();
        Partida partida = new Partida(mapa, new Posicion(2, 1));

        Punto comun = mapa.obtenerCelda(new Posicion(1, 1)).retirarPunto();
        partida.consumir(comun);
        partida.consumir(new SuperPunto());
        int puntaje = partida.getPuntaje();
        verificar("Puntaje antes de la perdida", 60, puntaje);

        partida.perderVida();

        verificar("El puntaje se conserva", puntaje, partida.getPuntaje());
        verificar("El punto consumido sigue retirado",
            false, mapa.obtenerCelda(new Posicion(1, 1)).tienePunto());
        verificar("Quedan puntos para continuar", true, mapa.quedanPuntos());
    }

    private static void continuarConVidas() {
        Laberinto mapa = mapa();
        Partida partida = new Partida(mapa, new Posicion(2, 1));

        partida.perderVida();
        verificar("Quedan dos vidas", 2, partida.getVidas());
        verificar("Sigue EN_CURSO", EstadoPartida.EN_CURSO, partida.getEstado());

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("PacMan vuelve a avanzar", 2,
            partida.getPosicionPacMan().columna);
        verificar("La partida no reinicia las vidas", 2, partida.getVidas());
    }

    private static void derrota() {
        Laberinto mapa = mapa();
        Partida partida = new Partida(mapa, new Posicion(2, 1));
        Fantasma[] fantasmas = fantasmas(partida);

        partida.perderVida();
        partida.perderVida();
        verificar("Una vida antes de la derrota", 1, partida.getVidas());
        verificar("Todavia EN_CURSO", EstadoPartida.EN_CURSO, partida.getEstado());

        partida.perderVida();
        verificar("Vidas al llegar a cero", 0, partida.getVidas());
        verificar("Estado final", EstadoPartida.DERROTA, partida.getEstado());

        Posicion pacManAntes = partida.getPosicionPacMan();
        Posicion[] fantasmasAntes = partida.getPosicionesFantasmas();
        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("PacMan no avanza tras la derrota", true,
            mismaPosicion(pacManAntes, partida.getPosicionPacMan()));
        for (int i = 0; i < fantasmas.length; i++) {
            verificar("Fantasma " + i + " no avanza tras la derrota", true,
                mismaPosicion(fantasmasAntes[i], fantasmas[i].getPosicion()));
        }

        partida.perderVida();
        verificar("Perder de nuevo no baja de cero", 0, partida.getVidas());
        verificar("La derrota no cambia", EstadoPartida.DERROTA, partida.getEstado());
    }

    private static void comprobarFantasmas(String descripcion, Partida partida,
            Posicion centro) {
        Fantasma[] fantasmas = fantasmas(partida);
        verificar(descripcion + " - cantidad", 4, fantasmas.length);
        for (int i = 0; i < fantasmas.length; i++) {
            Fantasma fantasma = fantasmas[i];
            verificar(descripcion + " - fantasma " + i + " en el centro", true,
                mismaPosicion(fantasma.getPosicion(), centro));
            verificar(descripcion + " - fantasma " + i + " estado",
                EstadoFantasma.PERSIGUIENDO, fantasma.getEstado());
            verificar(descripcion + " - fantasma " + i + " sin susto",
                0.0, leerTiempo(fantasma));
        }
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

    private static PacMan pacMan(Partida partida) {
        try {
            Field campo = Partida.class.getDeclaredField("pacMan");
            campo.setAccessible(true);
            return (PacMan) campo.get(partida);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo consultar PacMan", error);
        }
    }

    private static double leerTiempo(Fantasma fantasma) {
        try {
            Field campo = Fantasma.class.getDeclaredField("tiempoSustoRestante");
            campo.setAccessible(true);
            return campo.getDouble(fantasma);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo consultar el contador", error);
        }
    }

    private static boolean mismaPosicion(Posicion primera, Posicion segunda) {
        return primera.fila == segunda.fila && primera.columna == segunda.columna;
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        pruebas++;
        boolean coincide = Objects.equals(esperado, obtenido);
        if (coincide) {
            aprobadas++;
        }
        System.out.println((coincide ? "[OK] " : "[FALLA] ") + descripcion
            + " | esperado: " + esperado + " | obtenido: " + obtenido);
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
}
