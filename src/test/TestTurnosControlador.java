package test;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoPartida;
import extra.Posicion;
import modelo.Laberinto;
import modelo.Partida;

/** Pruebas de acciones del jugador, preparadas con asistencia de IA. */
public class TestTurnosControlador {

    public static void main(String[] args)
    {
        ControladorJuego controlador = new ControladorJuego();
        controlador.avanzarTurno(Direccion.IZQUIERDA);
        posicion(controlador, 13, 6);
        controlador.avanzarTurno();
        posicion(controlador, 13, 5);
        verificar(controlador.getTicks() == 2, "Cada accion avanza un solo tick");
        tiempo(controlador, 2.0 / 24.0);
        verificar(!controlador.nuevaPartida(), "No reinicia durante el juego");
        verificar(controlador.getTicks() == 2, "Reinicio rechazado conserva ticks");
        tiempo(controlador, 2.0 / 24.0);

        controlador.alternarPausa();
        verificar(controlador.getEstado() == EstadoPartida.PAUSADA, "Pausa");
        controlador.avanzarTurno(Direccion.DERECHA);
        controlador.avanzarTurno();
        controlador.moverPacMan(Direccion.DERECHA);
        controlador.actualizar(10);
        verificar(!controlador.nuevaPartida(), "No reinicia en pausa");
        posicion(controlador, 13, 5);
        verificar(controlador.getTicks() == 2, "Pausa conserva ticks");
        tiempo(controlador, 2.0 / 24.0);
        controlador.alternarPausa();
        controlador.avanzarTurno();
        posicion(controlador, 13, 4);
        verificar(controlador.getTicks() == 3, "Reanuda sin avanzar un tick extra");
        tiempo(controlador, 3.0 / 24.0);

        // Una pared impide desplazarse, pero el turno sigue consumiendo tiempo.
        controlador = new ControladorJuego();
        controlador.avanzarTurno(Direccion.ABAJO);
        posicion(controlador, 13, 7);
        verificar(controlador.getTicks() == 1, "Cuenta el turno contra una pared");
        controlador.actualizar(0.5);
        verificar(controlador.getTicks() == 2, "La API anterior tambien cuenta avances");
        tiempo(controlador, 1.0 / 24.0 + 0.5);

        Partida derrota = new Partida();
        derrota.perderVida();
        derrota.perderVida();
        derrota.perderVida();
        comprobarFinal(new ControladorJuego(derrota), EstadoPartida.DERROTA);

        Laberinto vacio = new Laberinto(new String[] {
            "###########", "#         #", "#         #", "#         #",
            "#         #", "#         #", "###########"
        });
        controlador = new ControladorJuego(new Partida(vacio, new Posicion(1, 1)));
        controlador.avanzarTurno();
        verificar(controlador.getTicks() == 1, "Cuenta el turno que termina la partida");
        comprobarFinal(controlador, EstadoPartida.VICTORIA);
        System.out.println("TestTurnosControlador: todas las comprobaciones OK");
    }

    private static void comprobarFinal(ControladorJuego controlador, EstadoPartida estado)
    {
        long ticks = controlador.getTicks();
        double tiempo = controlador.getTiempoSimulado();
        Posicion posicion = controlador.getPosicionPacMan();
        controlador.alternarPausa();
        controlador.avanzarTurno(Direccion.IZQUIERDA);
        controlador.avanzarTurno();
        verificar(controlador.getEstado() == estado, "No reabre " + estado);
        posicion(controlador, posicion.fila, posicion.columna);
        verificar(controlador.getTicks() == ticks, "No avanza tras " + estado);
        tiempo(controlador, tiempo);
        verificar(controlador.nuevaPartida(), "Permite reiniciar tras " + estado);
        verificar(controlador.getEstado() == EstadoPartida.EN_CURSO, "Nueva partida en curso");
        verificar(controlador.getTicks() == 0, "Reinicio limpia ticks");
        tiempo(controlador, 0);
        controlador.avanzarTurno();
        posicion(controlador, 13, 8);
        verificar(controlador.getTicks() == 1, "La partida nueva permite jugar");
    }

    private static void posicion(ControladorJuego controlador, int fila, int columna)
    {
        Posicion posicion = controlador.getPosicionPacMan();
        verificar(posicion.fila == fila && posicion.columna == columna,
            "Posicion esperada: (" + fila + "," + columna + ")");
    }

    private static void tiempo(ControladorJuego controlador, double esperado)
    {
        verificar(Math.abs(controlador.getTiempoSimulado() - esperado) < 1e-9,
            "Tiempo simulado esperado: " + esperado);
    }

    private static void verificar(boolean condicion, String descripcion)
    {
        if (!condicion) {
            throw new AssertionError(descripcion);
        }
    }
}
