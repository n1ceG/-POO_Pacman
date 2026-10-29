package consola;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.util.Locale;
import java.util.Scanner;

/** Vista de consola por pasos, creada con asistencia de IA a pedido de Juan. */
public class JuegoConsola {

    private static final double SEGUNDOS_POR_TICK = 1.0 / 24.0;
    private final ControladorJuego controlador = new ControladorJuego();
    private long ticks;
    private String mensaje = "Partida nueva. PacMan comienza hacia la derecha.";

    public static void main(String[] args)
    {
        new JuegoConsola().ejecutar();
    }

    private void ejecutar()
    {
        try (Scanner entrada = new Scanner(System.in)) {
            while (true) {
                mostrar();
                System.out.print("Comando > ");
                if (!entrada.hasNextLine()) {
                    break;
                }
                String comando = entrada.nextLine().trim().toLowerCase(Locale.ROOT);
                if (comando.equals("q")) {
                    break;
                }
                procesar(comando);
            }
        }
        System.out.println("Juego cerrado.");
    }

    private void procesar(String comando)
    {
        mensaje = "";
        if (comando.equals("p")) {
            if (controlador.getEstado() == EstadoPartida.PAUSADA) {
                controlador.reanudar();
            } else {
                controlador.pausar();
            }
            return;
        }
        if (comando.equals("n")) {
            if (finalizada()) {
                controlador.nuevaPartida();
                ticks = 0;
                mensaje = "Partida nueva.";
            } else {
                mensaje = "Solo podes iniciar otra partida despues de ganar o perder.";
            }
            return;
        }
        Direccion direccion = switch (comando) {
            case "w" -> Direccion.ARRIBA;
            case "s" -> Direccion.ABAJO;
            case "a" -> Direccion.IZQUIERDA;
            case "d" -> Direccion.DERECHA;
            default -> null;
        };
        if (!comando.isEmpty() && direccion == null) {
            mensaje = "Comando invalido. Usa una sola letra y Enter.";
            return;
        }
        if (controlador.getEstado() != EstadoPartida.EN_CURSO) {
            mensaje = finalizada() ? "Partida finalizada. N: nueva partida; Q: salir."
                : "Partida pausada. P: reanudar.";
            return;
        }
        if (direccion != null) {
            controlador.moverPacMan(direccion);
        }
        int vidasAntes = controlador.getVidas();
        int puntosAntes = controlador.getPuntaje();
        controlador.actualizar(SEGUNDOS_POR_TICK);
        ticks++;
        int puntosGanados = controlador.getPuntaje() - puntosAntes;
        if (puntosGanados > 0) {
            mensaje = "+" + puntosGanados + " puntos. ";
        }
        if (controlador.getVidas() < vidasAntes) {
            mensaje += "Perdiste una vida.";
        }
    }

    private boolean finalizada()
    {
        return controlador.getEstado() == EstadoPartida.VICTORIA
            || controlador.getEstado() == EstadoPartida.DERROTA;
    }

    private void mostrar()
    {
        // ANSI: limpiar la pantalla y llevar el cursor a la esquina superior.
        System.out.print("\033[2J\033[H");
        System.out.flush();
        System.out.printf(Locale.ROOT,
            "%n=== PAC-MAN | %s | Vidas: %d | Puntos: %d | Tick: %d | Tiempo: %.3f s ===%n",
            controlador.getEstado(), controlador.getVidas(), controlador.getPuntaje(),
            ticks, ticks * SEGUNDOS_POR_TICK);
        char[][] mapa = controlador.getMapa();
        Posicion[] fantasmas = controlador.getPosicionesFantasmas();
        EstadoFantasma[] estados = controlador.getEstadosFantasmas();
        // Si se superponen, mostrar primero el estado que puede hacer dano.
        for (EstadoFantasma estado : new EstadoFantasma[] {
            EstadoFantasma.REGRESANDO, EstadoFantasma.ASUSTADO, EstadoFantasma.PERSIGUIENDO
        }) {
            for (int i = 0; i < fantasmas.length; i++) {
                if (estados[i] == estado) {
                    mapa[fantasmas[i].fila][fantasmas[i].columna] = switch (estado) {
                        case PERSIGUIENDO -> 'F';
                        case ASUSTADO -> 'f';
                        case REGRESANDO -> 'r';
                    };
                }
            }
        }
        Posicion pacman = controlador.getPosicionPacMan();
        mapa[pacman.fila][pacman.columna] = 'P';
        for (char[] fila : mapa) {
            for (char celda : fila) {
                System.out.print(celda + " ");
            }
            System.out.println();
        }
        System.out.println("C: PacMan | F: persiguiendo | f: asustado | r: regresando | o: superpunto");
        for (int i = 0; i < fantasmas.length; i++) {
            System.out.printf("F%d: %s (%d,%d)%s", i + 1, estados[i],
                fantasmas[i].fila, fantasmas[i].columna, i == fantasmas.length - 1 ? "\n" : " | ");
        }
        System.out.println("W/A/S/D + Enter: direccion y tick | Enter: tick | P: pausa/reanuda | N: nueva | Q: salir");
        if (controlador.getEstado() == EstadoPartida.VICTORIA) {
            System.out.println("Ganaste! N: nueva partida; Q: salir.");
        } else if (controlador.getEstado() == EstadoPartida.DERROTA) {
            System.out.println("Sin vidas. N: nueva partida; Q: salir.");
        }
        if (!mensaje.isEmpty()) {
            System.out.println(mensaje);
        }
    }
}
