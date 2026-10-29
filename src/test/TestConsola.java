package test;

import controlador.ControladorJuego;
import extra.EstadoFantasma;
import extra.Posicion;
import modelo.Fantasma;

/** Pruebas de contratos para la consola, creadas con asistencia de IA. */
public class TestConsola {
    public static void main(String[] args)
    {
        ControladorJuego controlador = new ControladorJuego();
        char[][] mapa = controlador.getMapa();
        verificar(mapa.length == 15 && mapa[0].length == 15, "Dimensiones del mapa");
        mapa[0][0] = ' ';
        verificar(controlador.getMapa()[0][0] == '#', "El mapa devuelto es una copia");
        verificar(controlador.getMapa()[13][8] == '.', "Punto inicial visible");
        controlador.actualizar(1.0 / 24.0);
        verificar(controlador.getMapa()[13][8] == ' ', "El mapa refleja puntos consumidos");
        verificar(controlador.getPosicionPacMan().columna == 8, "Un tick mueve una celda");
        Fantasma fantasma = new Fantasma(new Posicion(7, 7));
        fantasma.asustar(10);
        for (int i = 0; i < 239; i++) {
            fantasma.actualizarEstado(1.0 / 24.0);
        }
        verificar(fantasma.getEstado() == EstadoFantasma.ASUSTADO, "Susto activo tras 239 ticks");
        fantasma.actualizarEstado(1.0 / 24.0);
        verificar(fantasma.getEstado() == EstadoFantasma.PERSIGUIENDO, "Susto termina tras 240 ticks");
        System.out.println("TestConsola: 7/7 comprobaciones OK");
    }

    private static void verificar(boolean condicion, String descripcion)
    {
        if (!condicion) {
            throw new AssertionError(descripcion);
        }
    }
}
