package test;

import extra.Direccion;
import extra.Posicion;
import modelo.Laberinto;
import modelo.Partida;
import extra.EstadoPartida;

public class TestConsumo {

    public static void main(String[] args)
    {
        Laberinto laberinto = new Laberinto(new String[] {
            "####",
            "# .#",
            "#.##",
            "####"
        });

        Partida partida = new Partida(
            laberinto,
            new Posicion(1, 1)
        );

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        if (partida.getPuntaje() != 10) {
            throw new AssertionError(
                "Se esperaban 10 puntos, pero hay " + partida.getPuntaje()   
            );
        }

        if (laberinto.obtenerCelda(new Posicion(1,2)).tienePunto()) {
            throw new AssertionError("El punto consumido sigue en la celda");
        }

        partida.actualizar(1);

        if (partida.getPuntaje() != 10) {
            throw new AssertionError(
                "Quedarse en la celda cambio el puntaje a " + partida.getPuntaje()
            );
        }

        partida.elegirDireccionPacman(Direccion.IZQUIERDA);
        partida.actualizar(1);
        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        if (partida.getPuntaje() != 10) {
            throw new AssertionError(
                "Volver a la celda cambio el puntaje a " + partida.getPuntaje()
            );
        }

        System.out.println("[OK] Volver a la celda no vuelva sumar");

        System.out.println("[OK] Quedarse en la celda no vuelve a sumar.");

        System.out.println("[OK] El punto comun suma 10 y desaparece");

        Laberinto laberintoSuper = new Laberinto(new String[] {
            "####",
            "# o#",
            "#.##",
            "####"
        });

        Partida partidaSuper = new Partida(
            laberintoSuper,
            new Posicion(1, 1)
        );

        partidaSuper.elegirDireccionPacman(Direccion.DERECHA);
        partidaSuper.actualizar(1);

        if (partidaSuper.getPuntaje() != 50) {
            throw new AssertionError(
                "Se esperaban 50 puntos, pero hay "
                + partidaSuper.getPuntaje()
            );
        }

        if (laberintoSuper.obtenerCelda(new Posicion(1, 2)).tienePunto()) {
            throw new AssertionError(
                "El superpunto consumido sigue en la celda"
            );
        }

        partidaSuper.actualizar(1);

        if (partidaSuper.getPuntaje() != 50) {
            throw new AssertionError(
                "Quedarse en la celda del superpunto volvio a sumar"
            );
        }

        partidaSuper.elegirDireccionPacman(Direccion.IZQUIERDA);
        partidaSuper.actualizar(1);

        partidaSuper.elegirDireccionPacman(Direccion.DERECHA);
        partidaSuper.actualizar(1);

        if (partidaSuper.getPuntaje() != 50) {
            throw new AssertionError(
                "Volver a la celda del superpunto volvio a sumar"
            );
        }

        System.out.println(
            "[OK] El superpunto no vuelve a sumar al quedarse ni al regresar."
        );

        System.out.println("[OK] El superpunto suma 50 y desaparece.");

        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            throw new AssertionError(
                "La prueba del punto comun debe mantener la partida EN_CURSO"
            );
        }

        if (partidaSuper.getEstado() != EstadoPartida.EN_CURSO) {
            throw new AssertionError(
                "La prueba del superpunto debe mantener la partida EN_CURSO"
            );
        }

        System.out.println(
            "[OK] Las pruebas de consumo mantienen la partida EN_CURSO"
        );
    }
    
}
