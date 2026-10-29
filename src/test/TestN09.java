package test;

import extra.EstadoFantasma;
import java.lang.reflect.Field;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Partida;
import modelo.PuntoComun;
import modelo.SuperPunto;


public class TestN09 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("El superpunto suma 50 y activa 10 segundos", TestN09::activarSusto);
        ejecutar("Otro superpunto reinicia a 10 segundos", TestN09::reiniciarSusto);
        ejecutar("Un fantasma REGRESANDO conserva estado y temporizador", TestN09::respetarRegreso);
        ejecutar("Un punto comun no activa el susto", TestN09::puntoComunNoActiva);
        ejecutar("Un punto comun no renueva el susto", TestN09::puntoComunNoRenueva);
        ejecutar("El susto vence a los 10 segundos", TestN09::vencimiento);
        System.out.println("\nN09: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N09 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static void activarSusto() {
        Partida partida = new Partida();
        partida.consumir(new SuperPunto());

        verificar("Puntaje tras un superpunto", 50, partida.getPuntaje());
        comprobarTodos("Tras consumir un superpunto", partida, EstadoFantasma.ASUSTADO, 10);
    }

    private static void reiniciarSusto() {
        Partida partida = new Partida();
        partida.consumir(new SuperPunto());

        for (Fantasma fantasma : fantasmas(partida)) {
            fantasma.actualizarEstado(4);
        }
        comprobarTodos("Cuatro segundos despues", partida, EstadoFantasma.ASUSTADO, 6);

        partida.consumir(new SuperPunto());

        verificar("El segundo superpunto suma otros 50", 100, partida.getPuntaje());
        comprobarTodos("Tras el segundo superpunto", partida, EstadoFantasma.ASUSTADO, 10);
    }

    private static void respetarRegreso() {
        Partida partida = new Partida();
        Fantasma[] fantasmas = fantasmas(partida);
        Fantasma enRegreso = fantasmas[1];
        enRegreso.asustar(10);
        enRegreso.serComido();
        comprobar("Antes del superpunto", enRegreso, EstadoFantasma.REGRESANDO, 0);

        partida.consumir(new SuperPunto());

        comprobar("El fantasma en regreso no cambia", enRegreso, EstadoFantasma.REGRESANDO, 0);
        for (int i = 0; i < fantasmas.length; i++) {
            if (i != 1) {
                comprobar("Fantasma " + i + " si se asusta", fantasmas[i],
                    EstadoFantasma.ASUSTADO, 10);
            }
        }
    }

    private static void puntoComunNoActiva() {
        Partida partida = new Partida();
        partida.consumir(new PuntoComun());

        verificar("Puntaje tras un punto comun", 10, partida.getPuntaje());
        comprobarTodos("Tras consumir un punto comun", partida,
            EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void puntoComunNoRenueva() {
        Partida partida = new Partida();
        partida.consumir(new SuperPunto());

        for (Fantasma fantasma : fantasmas(partida)) {
            fantasma.actualizarEstado(4);
        }
        partida.consumir(new PuntoComun());

        verificar("El punto comun suma 10", 60, partida.getPuntaje());
        comprobarTodos("El susto conserva sus 6 segundos", partida,
            EstadoFantasma.ASUSTADO, 6);
    }

    private static void vencimiento() {
        Partida partida = new Partida();
        partida.consumir(new SuperPunto());

        for (Fantasma fantasma : fantasmas(partida)) {
            fantasma.actualizarEstado(10);
        }

        comprobarTodos("Al cumplirse 10 segundos", partida,
            EstadoFantasma.PERSIGUIENDO, 0);
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

    private static void comprobarTodos(String descripcion, Partida partida,
            EstadoFantasma estadoEsperado, double tiempoEsperado) {
        Fantasma[] fantasmas = fantasmas(partida);
        verificar(descripcion + " - cantidad de fantasmas", 4, fantasmas.length);
        for (int i = 0; i < fantasmas.length; i++) {
            comprobar(descripcion + " - fantasma " + i, fantasmas[i],
                estadoEsperado, tiempoEsperado);
        }
    }

    private static void comprobar(String descripcion, Fantasma fantasma,
            EstadoFantasma estadoEsperado, double tiempoEsperado) {
        verificar(descripcion + " - estado", estadoEsperado, fantasma.getEstado());
        verificar(descripcion + " - segundos restantes", tiempoEsperado, leerTiempo(fantasma));
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
