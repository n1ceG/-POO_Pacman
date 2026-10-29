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
import modelo.PuntoComun;

public class TestN14 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("PERSIGUIENDO descuenta una vida y reinicia la partida", TestN14::persiguiendoPierdeVida);
        ejecutar("PERSIGUIENDO puede llevar la partida a DERROTA", TestN14::persiguiendoHastaDerrota);
        ejecutar("REGRESANDO no daña ni suma puntos", TestN14::regresandoInofensivo);
        ejecutar("Contactos con REGRESANDO no cambian su estado", TestN14::regresandoConservaEstado);
        System.out.println("\nN14: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N14 tiene fallas. Revisar los mensajes [FALLA].");
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

    private static Partida nuevaPartida() {
        return new Partida(mapa(), new Posicion(2, 1));
    }

    private static void persiguiendoPierdeVida() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        Fantasma[] fantasmas = fantasmas(partida);

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        int puntaje = controlador.getPuntaje();
        verificar("Preparacion: se consumio un punto", 10, puntaje);
        verificar("Preparacion: el fantasma persigue",
            EstadoFantasma.PERSIGUIENDO, fantasmas[0].getEstado());
        verificar("Preparacion: PacMan se alejo del inicio", 2,
            partida.getPosicionPacMan().columna);

        partida.resolverColision(fantasmas[0]);

        verificar("Una vida menos", 2, controlador.getVidas());
        verificar("La partida sigue EN_CURSO",
            EstadoPartida.EN_CURSO, controlador.getEstado());
        verificar("El puntaje no cambia por el contacto", puntaje,
            controlador.getPuntaje());
        verificar("PacMan reaparece en su inicio", true,
            mismaPosicion(new Posicion(2, 1), partida.getPosicionPacMan()));
        comprobarFantasmasReiniciados("Tras el contacto", partida, mapa().centro());
    }

    private static void persiguiendoHastaDerrota() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        Fantasma fantasma = fantasmas(partida)[0];

        for (int perdida = 1; perdida <= 2; perdida++) {
            partida.resolverColision(fantasma);
            verificar("Perdida " + perdida + ": vidas", 3 - perdida,
                controlador.getVidas());
            verificar("Perdida " + perdida + ": sigue EN_CURSO",
                EstadoPartida.EN_CURSO, controlador.getEstado());
        }

        partida.resolverColision(fantasma);

        verificar("Tercera perdida: vidas", 0, controlador.getVidas());
        verificar("Tercera perdida: DERROTA",
            EstadoPartida.DERROTA, controlador.getEstado());
    }

    private static void regresandoInofensivo() {
        Partida partida = nuevaPartida();
        ControladorJuego controlador = new ControladorJuego(partida);
        Fantasma fantasma = fantasmas(partida)[0];

        partida.consumir(new PuntoComun());
        int puntaje = controlador.getPuntaje();
        fantasma.asustar(10);
        fantasma.serComido();
        verificar("Preparacion: el fantasma regresa",
            EstadoFantasma.REGRESANDO, fantasma.getEstado());
        Posicion pacMan = partida.getPosicionPacMan();

        for (int contacto = 1; contacto <= 3; contacto++) {
            partida.resolverColision(fantasma);
            verificar("Contacto " + contacto + " con REGRESANDO: vidas", 3,
                controlador.getVidas());
            verificar("Contacto " + contacto + " con REGRESANDO: puntaje", puntaje,
                controlador.getPuntaje());
            verificar("Contacto " + contacto + " con REGRESANDO: PacMan quieto", true,
                mismaPosicion(pacMan, partida.getPosicionPacMan()));
        }
    }

    private static void regresandoConservaEstado() {
        Partida partida = nuevaPartida();
        Fantasma[] fantasmas = fantasmas(partida);
        Fantasma fantasma = fantasmas[0];
        fantasma.asustar(10);
        fantasma.serComido();
        verificar("Preparacion: en su origen y REGRESANDO", true,
            mismaPosicion(partida.getPosicionesFantasmas()[0], mapa().centro()));

        for (int contacto = 1; contacto <= 3; contacto++) {
            partida.resolverColision(fantasma);
            verificar("Contacto " + contacto + ": sigue REGRESANDO",
                EstadoFantasma.REGRESANDO, fantasma.getEstado());
            verificar("Contacto " + contacto + ": sin temporizador de susto",
                0.0, leerTiempo(fantasma));
        }

        for (int i = 1; i < fantasmas.length; i++) {
            verificar("El otro fantasma " + i + " conserva su estado",
                EstadoFantasma.PERSIGUIENDO, fantasmas[i].getEstado());
        }
    }

    private static void comprobarFantasmasReiniciados(String descripcion,
            Partida partida, Posicion centro) {
        Fantasma[] fantasmas = fantasmas(partida);
        for (int i = 0; i < fantasmas.length; i++) {
            verificar(descripcion + " - fantasma " + i + " en el centro", true,
                mismaPosicion(fantasmas[i].getPosicion(), centro));
            verificar(descripcion + " - fantasma " + i + " persigue",
                EstadoFantasma.PERSIGUIENDO, fantasmas[i].getEstado());
            verificar(descripcion + " - fantasma " + i + " sin susto",
                0.0, leerTiempo(fantasmas[i]));
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
