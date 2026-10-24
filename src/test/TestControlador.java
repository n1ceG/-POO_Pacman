/*package test;

public class TestControlador {

    public static void main(String[] args)
    {}
}*/

package test;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.Posicion;
import modelo.Laberinto;
import modelo.Partida;

public class TestControlador {

    private static int pruebas = 0;
    private static int aprobadas = 0;

    public static void main(String[] args)
    {
        System.out.println("=== B05: Movimiento de PacMan y paredes (R1-R2) ===");

        System.out.println("\n-- Cuatro direcciones desde el cruce (3,5) --");
        probarDireccion(Direccion.ARRIBA, 2, 5);
        probarDireccion(Direccion.ABAJO, 4, 5);
        probarDireccion(Direccion.IZQUIERDA, 3, 4);
        probarDireccion(Direccion.DERECHA, 3, 6);

        System.out.println("\n-- Sin dirección elegida --");
        ControladorJuego controlador = new ControladorJuego();
        controlador.actualizar(1);
        verificar("Sin tecla, PacMan no se mueve", 13, 7, controlador);

        System.out.println("\n-- Pared --");
        controlador = new ControladorJuego();
        controlador.moverPacMan(Direccion.ABAJO);
        controlador.actualizar(1);
        verificar("ABAJO hacia pared (14,7), se queda", 13, 7, controlador);

        System.out.println("\n-- Persistencia de la dirección --");
        controlador = new ControladorJuego();
        controlador.moverPacMan(Direccion.IZQUIERDA);
        for (int i = 0; i < 3; i++) {
            controlador.actualizar(1);
        }
        verificar("IZQUIERDA con 3 ticks avanza 3 celdas", 13, 4, controlador);

        controlador = new ControladorJuego();
        controlador.moverPacMan(Direccion.IZQUIERDA);
        for (int i = 0; i < 10; i++) {
            controlador.actualizar(1);
        }
        verificar("IZQUIERDA con 10 ticks frena en la pared", 13, 1, controlador);

        System.out.println("\n-- Borde del laberinto --");
        Laberinto mapaConBorde = new Laberinto(new String[] {
            "#.#",
            "#.#",
            "###"
        });
        controlador = new ControladorJuego(
            new Partida(mapaConBorde, new Posicion(0, 1))
        );
        controlador.moverPacMan(Direccion.ARRIBA);
        controlador.actualizar(1);
        verificar("ARRIBA fuera de la matriz, se queda", 0, 1, controlador);

        System.out.println("\n-- Giro alineado con la celda --");
        controlador = new ControladorJuego(
            new Partida(Laberinto.crearDefault(), new Posicion(13, 6))
        );
        controlador.moverPacMan(Direccion.ARRIBA);
        controlador.actualizar(1);
        verificar("Giro ARRIBA hacia pared (12,6), se queda", 13, 6, controlador);

        controlador = new ControladorJuego();
        controlador.moverPacMan(Direccion.ARRIBA);
        controlador.actualizar(1);
        verificar("Giro ARRIBA hacia camino (12,7), gira", 12, 7, controlador);

        System.out.println("\nResultado: " + aprobadas + "/" + pruebas + " pruebas OK");
    }

    private static void probarDireccion(
        Direccion direccion,
        int filaEsperada,
        int columnaEsperada
    )
    {
        ControladorJuego controlador = new ControladorJuego(
            new Partida(Laberinto.crearDefault(), new Posicion(3, 5))
        );
        controlador.moverPacMan(direccion);
        controlador.actualizar(1);
        verificar(direccion + " desde (3,5)", filaEsperada, columnaEsperada, controlador);
    }

    private static void verificar(
        String descripcion,
        int filaEsperada,
        int columnaEsperada,
        ControladorJuego controlador
    )
    {
        Posicion obtenida = controlador.getPosicionPacMan();
        boolean ok = obtenida.fila == filaEsperada &&
            obtenida.columna == columnaEsperada;

        pruebas++;
        if (ok) {
            aprobadas++;
        }

        System.out.println(
            (ok ? "[OK]    " : "[FALLA] ") + descripcion +
            " -> esperado (" + filaEsperada + "," + columnaEsperada + ")" +
            ", obtenido (" + obtenida.fila + "," + obtenida.columna + ")"
        );
    }
}
