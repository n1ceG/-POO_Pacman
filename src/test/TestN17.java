package test;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.PacMan;
import modelo.Partida;
import modelo.Punto;

/**
 * Pruebas de N17 preparadas con asistencia de IA. Sin JUnit ni -ea.
 * Desde TPO-Pacman: compilar src con --release 17 -encoding UTF-8 -d bin;
 * ejecutar: java -cp bin test.TestN17
 * Las ordenes de pausa, reanudacion y actualizacion pasan por el controlador.
 * Reflection consulta personajes y temporizadores sin agregar API al modelo.
 * Solo se escribe estado por reflection para preparar estados finales aislados:
 * no se prueba aqui como se gana o pierde (N12/N16), sino que no se reabran.
 * Se verifica tambien una victoria obtenida mediante actualizar().
 * El descuento del susto durante el juego activo es de N15/N19: estos casos
 * comprueban su conservacion en pausa, sin declarar integrada esa cuenta atras.
 */
public class TestN17 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Transiciones y ordenes repetidas", TestN17::transiciones);
        ejecutar("Pausa conserva una partida ya avanzada", TestN17::conservacion);
        ejecutar("Varios ciclos de pausa y continuacion", TestN17::ciclos);
        ejecutar("Pausa con mapa sin puntos", TestN17::pausaSinPuntos);
        ejecutar("VICTORIA y DERROTA no se reabren", TestN17::estadosFinales);
        ejecutar("Victoria obtenida jugando no se reabre", TestN17::victoriaReal);
        System.out.println("\nN17: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        System.out.println("Alcance: conserva el susto en pausa; no valida su descuento activo (N15/N19).");
        if (pruebas != aprobadas) {
            throw new AssertionError("N17 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static void transiciones() {
        Escenario e = new Escenario(false);
        Captura inicial = capturar(e);
        verificar("Inicio", EstadoPartida.EN_CURSO, e.controlador.getEstado());
        e.controlador.reanudar();
        verificar("Reanudar estando en curso", EstadoPartida.EN_CURSO, e.controlador.getEstado());
        conservar("Reanudacion sin pausa previa", inicial, e);
        for (int intento = 1; intento <= 2; intento++) {
            e.controlador.pausar();
            verificar("Pausar, intento " + intento, EstadoPartida.PAUSADA, e.controlador.getEstado());
            conservar("Pausar, intento " + intento, inicial, e);
        }
        for (int intento = 1; intento <= 2; intento++) {
            e.controlador.reanudar();
            verificar("Reanudar, intento " + intento, EstadoPartida.EN_CURSO, e.controlador.getEstado());
            conservar("Reanudar, intento " + intento, inicial, e);
        }
    }

    private static void conservacion() {
        Escenario e = new Escenario(false);
        // Primer paso: superpunto en (1,2); segundo: punto comun en (2,2).
        e.controlador.actualizar(0.25);
        e.controlador.moverPacMan(Direccion.ABAJO);
        e.controlador.actualizar(0.25);
        Fantasma[] fantasmas = fantasmas(e.partida);
        fantasmas[0].actualizarEstado(2.5); // Susto parcialmente consumido: 7.5 s.
        fantasmas[1].serComido();
        fantasmas[2].actualizarEstado(10); // Mezcla de los tres estados.
        verificar("Preparacion: PacMan avanzo", "(2,2)", posicion(e.controlador.getPosicionPacMan()));
        verificar("Preparacion: puntaje acumulado", true, e.controlador.getPuntaje() > 0);
        verificar("Preparacion: superpunto consumido", false,
            e.mapa.obtenerCelda(new Posicion(1, 2)).tienePunto());
        verificar("Preparacion: punto comun consumido", false,
            e.mapa.obtenerCelda(new Posicion(2, 2)).tienePunto());
        verificar("Preparacion: susto parcial", 7.5, tiempo(fantasmas[0]));
        verificar("Preparacion: regreso", EstadoFantasma.REGRESANDO, fantasmas[1].getEstado());
        verificar("Preparacion: perseguidor", EstadoFantasma.PERSIGUIENDO, fantasmas[2].getEstado());
        Captura antes = capturar(e);

        e.controlador.pausar();
        conservar("Al pausar", antes, e);
        for (double segundos : new double[] {0, 0.5, 20, 1000}) {
            e.controlador.actualizar(segundos);
            verificar("Actualizar " + segundos + " s conserva PAUSADA",
                EstadoPartida.PAUSADA, e.controlador.getEstado());
            conservar("Pausa, actualizar " + segundos + " s", antes, e);
        }
        e.controlador.reanudar();
        verificar("Reanudacion", EstadoPartida.EN_CURSO, e.controlador.getEstado());
        conservar("Antes del primer paso reanudado", antes, e);
        int puntajePrevio = e.controlador.getPuntaje();
        int valorSiguiente = e.mapa.obtenerCelda(new Posicion(3, 2)).tipoPunto().valor();
        e.controlador.actualizar(0.5);
        verificar("Continua desde donde estaba y en la misma direccion", "(3,2)",
            posicion(e.controlador.getPosicionPacMan()));
        verificar("Continua acumulando puntos", puntajePrevio + valorSiguiente, e.controlador.getPuntaje());
        verificar("Consume la siguiente celda", false,
            e.mapa.obtenerCelda(new Posicion(3, 2)).tienePunto());
        verificar("Conserva vidas al continuar sin contacto", antes.vidas, e.controlador.getVidas());
    }

    private static void ciclos() {
        Escenario e = new Escenario(false);
        for (int ciclo = 1; ciclo <= 3; ciclo++) {
            Captura antes = capturar(e);
            e.controlador.pausar();
            e.controlador.actualizar(50);
            verificar("Ciclo " + ciclo + ": queda pausada", EstadoPartida.PAUSADA, e.controlador.getEstado());
            conservar("Ciclo " + ciclo + ": durante pausa", antes, e);
            e.controlador.reanudar();
            verificar("Ciclo " + ciclo + ": reanuda", EstadoPartida.EN_CURSO, e.controlador.getEstado());
            conservar("Ciclo " + ciclo + ": al reanudar", antes, e);
            e.controlador.actualizar(0.1);
            verificar("Ciclo " + ciclo + ": vuelve a avanzar", "(1," + (ciclo + 1) + ")",
                posicion(e.controlador.getPosicionPacMan()));
        }
    }

    private static void pausaSinPuntos() {
        Escenario e = new Escenario(true);
        Captura antes = capturar(e);
        e.controlador.pausar();
        e.controlador.actualizar(100);
        verificar("Mapa vacio no convierte PAUSADA en VICTORIA",
            EstadoPartida.PAUSADA, e.controlador.getEstado());
        conservar("Mapa vacio pausado", antes, e);
        e.controlador.reanudar();
        verificar("Mapa vacio permite reanudar", EstadoPartida.EN_CURSO, e.controlador.getEstado());
        conservar("Mapa vacio al reanudar", antes, e);
    }

    private static void estadosFinales() {
        for (EstadoPartida estado : new EstadoPartida[] {EstadoPartida.VICTORIA, EstadoPartida.DERROTA}) {
            for (boolean sinPuntos : new boolean[] {false, true}) {
                Escenario e = new Escenario(sinPuntos);
                fijarEstado(e.partida, estado);
                comprobarFinal(e, estado + ", sin puntos=" + sinPuntos);
            }
        }
    }

    private static void victoriaReal() {
        Escenario e = new Escenario(true);
        e.controlador.actualizar(0.1);
        verificar("Preparacion: victoria por mapa sin puntos", EstadoPartida.VICTORIA, e.controlador.getEstado());
        comprobarFinal(e, "Victoria obtenida con actualizar");
    }

    private static void comprobarFinal(Escenario e, String caso) {
        EstadoPartida estado = e.controlador.getEstado();
        Captura antes = capturar(e);
        for (int intento = 1; intento <= 2; intento++) {
            e.controlador.reanudar();
            verificar(caso + ": reanudar " + intento, estado, e.controlador.getEstado());
            conservar(caso + ": despues de reanudar " + intento, antes, e);
            e.controlador.pausar();
            verificar(caso + ": pausar " + intento, estado, e.controlador.getEstado());
            conservar(caso + ": despues de pausar " + intento, antes, e);
            e.controlador.actualizar(100);
            verificar(caso + ": actualizar " + intento, estado, e.controlador.getEstado());
            conservar(caso + ": despues de actualizar " + intento, antes, e);
        }
    }

    private static Captura capturar(Escenario e) {
        PacMan pacman = (PacMan) leer(e.partida, "pacMan");
        Fantasma[] fantasmas = fantasmas(e.partida);
        StringBuilder posiciones = new StringBuilder();
        StringBuilder direcciones = new StringBuilder();
        double[] tiempos = new double[fantasmas.length];
        for (int i = 0; i < fantasmas.length; i++) {
            posiciones.append(posicion(e.controlador.getPosicionesFantasmas()[i])).append(' ');
            direcciones.append(fantasmas[i].getDireccion()).append(' ');
            tiempos[i] = tiempo(fantasmas[i]);
        }
        Punto[] puntos = new Punto[e.mapa.getFilas() * e.mapa.getColumnas()];
        int indice = 0;
        for (int fila = 0; fila < e.mapa.getFilas(); fila++) {
            for (int columna = 0; columna < e.mapa.getColumnas(); columna++) {
                puntos[indice++] = e.mapa.obtenerCelda(new Posicion(fila, columna)).tipoPunto();
            }
        }
        return new Captura(e.controlador.getVidas(), e.controlador.getPuntaje(),
            posicion(e.controlador.getPosicionPacMan()), pacman.getDireccion(),
            posiciones.toString(), direcciones.toString(), e.controlador.getEstadosFantasmas(),
            tiempos, puntos, pacman, fantasmas.clone(), leer(e.partida, "laberinto"));
    }

    private static void conservar(String caso, Captura antes, Escenario e) {
        Captura despues = capturar(e);
        verificar(caso + ": vidas", antes.vidas, despues.vidas);
        verificar(caso + ": puntaje", antes.puntaje, despues.puntaje);
        verificar(caso + ": posicion PacMan", antes.posicionPacman, despues.posicionPacman);
        verificar(caso + ": direccion PacMan", antes.direccion, despues.direccion);
        verificar(caso + ": posiciones fantasmas", antes.posicionesFantasmas, despues.posicionesFantasmas);
        verificar(caso + ": direcciones fantasmas", antes.direccionesFantasmas, despues.direccionesFantasmas);
        verificar(caso + ": estados fantasmas", antes.estados, despues.estados);
        verificar(caso + ": segundos restantes de susto", antes.tiempos, despues.tiempos);
        verificar(caso + ": mismos puntos en las mismas celdas", true, Arrays.equals(antes.puntos, despues.puntos));
        verificar(caso + ": mismo PacMan", true, antes.pacman == despues.pacman);
        verificar(caso + ": mismos fantasmas", true, Arrays.equals(antes.fantasmas, despues.fantasmas));
        verificar(caso + ": mismo laberinto", true, antes.mapa == despues.mapa);
    }

    private record Captura(int vidas, int puntaje, String posicionPacman, Direccion direccion,
            String posicionesFantasmas, String direccionesFantasmas, EstadoFantasma[] estados,
            double[] tiempos, Punto[] puntos, PacMan pacman, Fantasma[] fantasmas, Object mapa) {}

    private static class Escenario {
        final Laberinto mapa;
        final Partida partida;
        final ControladorJuego controlador;

        Escenario(boolean sinPuntos) {
            String[] filas = {"###########", "#.o.......#", "#.........#", "#.........#",
                "#.........#", "#.........#", "###########"};
            if (sinPuntos) {
                for (int i = 0; i < filas.length; i++) {
                    filas[i] = filas[i].replace('.', ' ').replace('o', ' ');
                }
            }
            mapa = new Laberinto(filas);
            partida = new Partida(mapa, new Posicion(1, 1));
            controlador = new ControladorJuego(partida);
        }
    }

    private static Fantasma[] fantasmas(Partida partida) {
        return (Fantasma[]) leer(partida, "fantasmas");
    }

    private static double tiempo(Fantasma fantasma) {
        return (double) leer(fantasma, "tiempoSustoRestante");
    }

    private static Object leer(Object objeto, String nombre) {
        try {
            Field campo = objeto.getClass().getDeclaredField(nombre);
            campo.setAccessible(true);
            return campo.get(objeto);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo consultar " + nombre, error);
        }
    }

    private static void fijarEstado(Partida partida, EstadoPartida estado) {
        try {
            Field campo = Partida.class.getDeclaredField("estado");
            campo.setAccessible(true);
            campo.set(partida, estado);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo preparar el estado final", error);
        }
    }

    private static String posicion(Posicion posicion) {
        return "(" + posicion.fila + "," + posicion.columna + ")";
    }

    private static String mostrar(Object valor) {
        if (valor instanceof Object[]) return Arrays.deepToString((Object[]) valor);
        if (valor instanceof double[]) return Arrays.toString((double[]) valor);
        return String.valueOf(valor);
    }

    private static void verificar(String caso, Object esperado, Object obtenido) {
        pruebas++;
        boolean ok = Objects.deepEquals(esperado, obtenido);
        if (ok) aprobadas++;
        System.out.println((ok ? "[OK] " : "[FALLA] ") + caso
            + " | esperado: " + mostrar(esperado) + " | obtenido: " + mostrar(obtenido));
    }

    private static void ejecutar(String nombre, Runnable caso) {
        System.out.println("\n-- " + nombre + " --");
        try {
            caso.run();
        } catch (RuntimeException | AssertionError error) {
            pruebas++;
            System.out.println("[FALLA] " + nombre + " | esperado: ejecucion sin excepciones | obtenido: " + error);
            error.printStackTrace(System.out);
        }
    }
}
