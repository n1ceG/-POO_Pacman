package test;

import extra.Direccion;
import extra.EstadoPartida;
import extra.Posicion;
import modelo.Laberinto;
import modelo.Partida;

public class TestVictoria {

    public static void main(String[] args)
    {
        Laberinto laberinto = new Laberinto(new String[] {
            "#####",
            "# .o#",
            "#####"
        });

        Partida partida = new Partida(
            laberinto,
            new Posicion(1,1)
        );

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        if (partida.comprobarVictoria()) {
            throw new AssertionError(
                "Todavia queda un superpunto: no deberia haber victoria"
            );
        }

        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            throw new AssertionError(
                "La partida deberia seguir EN_CURSO)"
            );
        }

        System.out.println(
            "[OK] La partida continua mientras queda un superpunto"
        );

        partida.actualizar(1);

        if (partida.getEstado() != EstadoPartida.VICTORIA) {
            throw new AssertionError(
                "Consumir el ultimo superpunto deberia declarar VICTORIA"
            );
        }

        if (partida.getPuntaje() != 60) {
            throw new AssertionError(
                "Se esperaban 60 puntos, pero hay "
                + partida.getPuntaje()
            );
        }

        System.out.println(
            "[OK] Consumir el ultimo superpunto declara VICTORIA con una puntuacion de " + partida.getPuntaje() + " puntos"
        );

        partida.elegirDireccionPacman(Direccion.IZQUIERDA);
        partida.actualizar(1);

        Posicion posicionFinal = partida.getPosicionPacMan();

        if(posicionFinal.fila != 1 || posicionFinal.columna != 3) {
            throw new AssertionError(
                "PacMan se movio despues de ganar"
            );
        }
        
        if (partida.getEstado() != EstadoPartida.VICTORIA) {
            throw new AssertionError(
                "La partida deberia conservar el estado VICTORIA"
            );
        }

        System.out.println(
            "[OK] PacMan no se mueve despues de ganar y la partida conserva el estado VICTORIA"
        );

        Laberinto laberintoComun = new Laberinto(new String[] {
            "#####",
            "# o.#",
            "#####"
        });

        Partida partidaComun = new Partida(
            laberintoComun,
            new Posicion(1,1)
        );

        partidaComun.elegirDireccionPacman(Direccion.DERECHA);
        partidaComun.actualizar(1);

        if (partidaComun.getEstado() != EstadoPartida.EN_CURSO) {
            throw new AssertionError(
                "Todavia queda un punto comun: la partida deberia seguir EN_CURSO"
            );
        }

        System.out.println(
            "[OK] La partida continua mientras queda un punto comun"
        );

        partidaComun.actualizar(1);

        if (partidaComun.getEstado() != EstadoPartida.VICTORIA) {
            throw new AssertionError(
                "Consumir el ultimo punto comun deberia declarar VICTORIA"
            );
        }

        if (partidaComun.getPuntaje() != 60) {
            throw new AssertionError(
                "Se esperaban 60 puntos, pero hay "
                + partidaComun.getPuntaje()
            );
        }

        System.out.println(
            "[OK] Consumir el ultimo punto comun declara VICTORIA con una puntuacion de " + partidaComun.getPuntaje() + " puntos"
        );
    }
}