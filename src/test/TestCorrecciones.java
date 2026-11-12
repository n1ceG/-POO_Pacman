package test;

import controlador.ControladorJuego;
import dto.Posicion;
import extra.Direccion;
import extra.EstadoFantasma;
import java.lang.reflect.Method;
import modelo.Celda;
import modelo.Laberinto;
import modelo.Partida;

/** Regresiones de las correcciones del 12/11/2026, preparadas con asistencia de IA. */
public class TestCorrecciones {
    private static int comprobaciones;

    public static void main(String[] args) {
        comprobarDTO();
        comprobarCeldasYMapa();
        comprobarParedes();
        comprobarSustoSwing();
        System.out.println("[OK] Correcciones: " + comprobaciones + " comprobaciones.");
    }

    private static void comprobarDTO() {
        ControladorJuego c = new ControladorJuego();
        Posicion posicion = c.getPosicionPacMan();
        posicion.setPosicion(0, 0);
        comprobar(c.getPosicionPacMan().fila == 13, "Modificar DTO no mueve a PacMan");
        Posicion[] fantasmas = c.getPosicionesFantasmas();
        fantasmas[0].setPosicion(0, 0);
        comprobar(c.getPosicionesFantasmas()[0].fila == 7, "Modificar DTO no mueve al fantasma");
        var foto = c.getPartidaDTO();
        var fantasma = c.getFantasmasDTO().get(0);
        c.avanzarTurno();
        comprobar(foto.getPacManColumna() == 7, "DTO conserva la foto anterior al turno");
        comprobar(fantasma.getFila() == 7 && fantasma.getColumna() == 5,
            "DTO del fantasma conserva la foto anterior");
        for (Method metodo : ControladorJuego.class.getDeclaredMethods()) {
            comprobar(!metodo.getGenericReturnType().getTypeName().contains("modelo."),
                "El controlador no devuelve entidades: " + metodo.getName());
        }
    }

    private static void comprobarCeldasYMapa() {
        Celda celda = new Celda(false, 50, 1, 2);
        celda.getPosicion().fila = 99;
        comprobar(celda.getPosicion().fila == 1, "Celda entrega una copia de su posicion");
        comprobar(celda.retirarPunto() == 50, "Superpunto entrega 50");
        comprobar(celda.retirarPunto() == 0 && !celda.tienePunto(), "No se consume dos veces");
        comprobar(new Celda(true, 50, 0, 0).retirarPunto() == 0, "Pared no entrega puntos");

        Laberinto laberinto = new Laberinto(new String[] {"#.o "});
        char[][] mapa = laberinto.getMapa();
        comprobar(new String(mapa[0]).equals("#.o "), "Laberinto representa los cuatro tipos de celda");
        mapa[0][0] = ' ';
        comprobar(laberinto.getMapa()[0][0] == '#', "Modificar mapa no modifica el laberinto");
        laberinto.obtenerCelda(new Posicion(0, 2)).retirarPunto();
        comprobar(laberinto.getMapa()[0][2] == ' ', "Mapa refleja consumo");
        comprobar(mapa[0][2] == 'o', "Una foto anterior no cambia al consumir");
    }

    private static void comprobarParedes() {
        // PacMan aislado; fantasmas en otra habitacion; punto de reserva evita victoria.
        for (Direccion direccion : Direccion.values()) {
            Partida partida = new Partida(new Laberinto(new String[] {
                "#############",
                "# #.#########",
                "#############",
                "#           #",
                "#           #",
                "#           #",
                "#############"
            }), new Posicion(1, 1));
            partida.elegirDireccionPacman(direccion);
            partida.actualizar(0.2);
            comprobar(partida.getPosicionPacMan().fila == 1
                && partida.getPosicionPacMan().columna == 1,
                "Partida impide atravesar pared hacia " + direccion);
        }
    }

    private static void comprobarSustoSwing() {
        Partida partida = new Partida(new Laberinto(new String[] {
            "#############",
            "# o.        #",
            "#############",
            "#           #",
            "#           #",
            "#           #",
            "#############"
        }), new Posicion(1, 1));
        ControladorJuego c = SoportePruebas.controlador(partida);
        c.actualizar(0.2);
        comprobar(c.getPuntaje() == 50, "Consumo de superpunto suma 50");
        c.moverPacMan(null);
        for (int i = 0; i < 49; i++) c.actualizar(0.2);
        for (EstadoFantasma estado : c.getEstadosFantasmas()) {
            comprobar(estado == EstadoFantasma.ASUSTADO, "Susto vigente a los 9.8 segundos");
        }
        c.actualizar(0.2);
        for (EstadoFantasma estado : c.getEstadosFantasmas()) {
            comprobar(estado == EstadoFantasma.PERSIGUIENDO, "Susto termina a los 10 segundos");
        }
    }

    private static void comprobar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) throw new AssertionError(mensaje);
    }
}
