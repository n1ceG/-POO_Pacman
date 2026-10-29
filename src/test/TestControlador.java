/*package test;

public class TestControlador {

    public static void main(String[] args)
    {}
}*/

package test;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoPartida;
import extra.Posicion;
import modelo.Laberinto;
import modelo.Partida;
import modelo.PacMan;

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

        System.out.println("\n-- Direccion inicial hacia la derecha --");
        ControladorJuego controlador = new ControladorJuego();
        controlador.actualizar(1);
        verificar("Sin tecla, PacMan avanza a la derecha", 13, 8, controlador);

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

        System.out.println("\n-- N02: Inicialización de la partida (R8) --");
        controlador = new ControladorJuego();
        verificar("Partida nueva tiene 3 vidas", controlador.getVidas() == 3);
        verificar("Partida nueva tiene 0 puntos", controlador.getPuntaje() == 0);
        verificar("Partida nueva está EN_CURSO",
            controlador.getEstado() == EstadoPartida.EN_CURSO);

        controlador = new ControladorJuego(
            new Partida(Laberinto.crearDefault(), new Posicion(3, 5))
        );
        verificar("Constructor con laberinto también inicia 3/0/EN_CURSO",
            controlador.getVidas() == 3 &&
            controlador.getPuntaje() == 0 &&
            controlador.getEstado() == EstadoPartida.EN_CURSO);

        ControladorJuego otro = new ControladorJuego();
        controlador.moverPacMan(Direccion.DERECHA);
        controlador.actualizar(1);
        verificar("Partidas distintas no comparten valores",
            otro.getVidas() == 3 &&
            otro.getPuntaje() == 0 &&
            otro.getEstado() == EstadoPartida.EN_CURSO);
        
        System.out.println("\n-- N21: Direcion inicial y regreso --");

        Laberinto laberinto = Laberinto.crearDefault();
        PacMan pacMan = new PacMan(new Posicion(3,5));

        verificar(
            "PacMan comienza mirando a la derecha",
            pacMan.getDireccion() == Direccion.DERECHA
        );

        pacMan.elegirDireccion(Direccion.IZQUIERDA);
        pacMan.mover(pacMan.getDireccion(), laberinto);

        verificar(
            "Antes del regreso, PacMan se desplazo a la izquierda",
            pacMan.getPosicion().fila == 3 &&
            pacMan.getPosicion().columna == 4
        );

        pacMan.volverAlInicio();

        verificar(
            "Volver al inicio recupera la posicion inical",
            pacMan.getPosicion().fila == 3 &&
            pacMan.getPosicion().columna == 5
        );

        verificar(
            "Volver al inicio recupera la direccion DERECHA",
            pacMan.getDireccion() == Direccion.DERECHA
        );
        
        System.out.println("\n-- N21: Inicio frente a una pared --");

        controlador = new ControladorJuego(
            new Partida(laberinto, new Posicion(1,13))
        );

        controlador.actualizar(1);

        verificar(
            "Sin nueva orden, la pared derecha impide avanzar",
            1,
            13,
            controlador
        );

        System.out.println("\n=== B06: Consumo de puntos y puntaje (R3) ===");
        probarConsumoPuntoComun();
        probarConsumoSuperPunto();
        probarAcumulacionPuntos();
        probarConsumirNulo();

        System.out.println("\nResultado: " + aprobadas + "/" + pruebas + " pruebas OK");

        if (aprobadas != pruebas) {
            throw new AssertionError(
                "Fallaron " + (pruebas - aprobadas) + " de " + pruebas + " pruebas"
            );
        }
    }

    private static void probarConsumoPuntoComun()
    {
        System.out.println("\n-- Punto común --");
        Laberinto mapa = new Laberinto(new String[] {
            "####",
            "# .#",
            "####"
        });
        ControladorJuego controlador = new ControladorJuego(
            new Partida(mapa, new Posicion(1, 1))
        );

        controlador.moverPacMan(Direccion.DERECHA);
        controlador.actualizar(1);

        verificar("Punto común suma 10", controlador.getPuntaje() == 10);
        verificar(
            "El punto común queda retirado",
            !mapa.obtenerCelda(new Posicion(1, 2)).tienePunto()
        );

        controlador.actualizar(1);
        verificar(
            "Quedarse en la celda no vuelve a sumar",
            controlador.getPuntaje() == 10
        );

        controlador.moverPacMan(Direccion.IZQUIERDA);
        controlador.actualizar(1);
        controlador.moverPacMan(Direccion.DERECHA);
        controlador.actualizar(1);
        verificar(
            "Volver a la celda no vuelve a sumar",
            controlador.getPuntaje() == 10
        );
    }

    private static void probarConsumoSuperPunto()
    {
        System.out.println("\n-- Superpunto --");
        Laberinto mapa = new Laberinto(new String[] {
            "####",
            "# o#",
            "####"
        });
        ControladorJuego controlador = new ControladorJuego(
            new Partida(mapa, new Posicion(1, 1))
        );

        controlador.moverPacMan(Direccion.DERECHA);
        controlador.actualizar(1);

        verificar("Superpunto suma 50", controlador.getPuntaje() == 50);
        verificar(
            "El superpunto queda retirado",
            !mapa.obtenerCelda(new Posicion(1, 2)).tienePunto()
        );

        controlador.actualizar(1);
        verificar(
            "Quedarse en el superpunto no vuelve a sumar",
            controlador.getPuntaje() == 50
        );

        controlador.moverPacMan(Direccion.IZQUIERDA);
        controlador.actualizar(1);
        controlador.moverPacMan(Direccion.DERECHA);
        controlador.actualizar(1);
        verificar(
            "Volver al superpunto no vuelve a sumar",
            controlador.getPuntaje() == 50
        );
    }

    private static void probarAcumulacionPuntos()
    {
        System.out.println("\n-- Acumulación de puntos --");
        Laberinto mapa = new Laberinto(new String[] {
            "#####",
            "# .o#",
            "#####"
        });
        ControladorJuego controlador = new ControladorJuego(
            new Partida(mapa, new Posicion(1, 1))
        );

        controlador.moverPacMan(Direccion.DERECHA);
        controlador.actualizar(1);
        verificar("El primer punto suma 10", controlador.getPuntaje() == 10);

        controlador.actualizar(1);
        verificar(
            "Punto común + superpunto acumulan 60",
            controlador.getPuntaje() == 60
        );
    }

    private static void probarConsumirNulo()
    {
        System.out.println("\n-- Consumir nulo --");
        Partida partida = new Partida(
            new Laberinto(new String[] {
                "###",
                "# #",
                "###"
            }),
            new Posicion(1, 1)
        );

        partida.consumir(null);
        verificar(
            "consumir(null) no modifica el puntaje",
            partida.getPuntaje() == 0
        );
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

    private static void verificar(String descripcion, boolean ok)
    {
        pruebas++;
        if (ok) {
            aprobadas++;
        }
        System.out.println((ok ? "[OK]    " : "[FALLA] ") + descripcion);
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
