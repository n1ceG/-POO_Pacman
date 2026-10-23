package modelo;

import extra.Direccion;
import extra.Posicion;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Laberinto {

    private static final String[] MAPA_POR_DEFECTO = {
        "###############",
        "#o...........o#",
        "#.###.###.###.#",
        "#.............#",
        "#.###.#.#.###.#",
        "#.....#.#.....#",
        "###.#.#.#.#.###",
        "#...#.....#...#",
        "#.#.#.###.#.#.#",
        "#...#.....#...#",
        "###.#.#.#.#.###",
        "#.....#.#.....#",
        "#.###.#.#.###.#",
        "#o...........o#",
        "###############"
    };

    private final Celda[][] celdas;

    public Laberinto() {
        this(MAPA_POR_DEFECTO);
    }

    public Laberinto(Celda[][] celdas) {
        if (
            celdas == null ||
            celdas.length == 0 ||
            celdas[0] == null ||
            celdas[0].length == 0
        ) {
            throw new IllegalArgumentException("El laberinto no puede estar vacío");
        }

        int columnas = celdas[0].length;
        for (Celda[] fila : celdas) {
            if (fila == null || fila.length != columnas) {
                throw new IllegalArgumentException(
                    "Todas las filas deben tener el mismo largo"
                );
            }
            for (Celda celda : fila) {
                if (celda == null) {
                    throw new IllegalArgumentException(
                        "Las celdas del laberinto no pueden ser nulas"
                    );
                }
            }
        }

        this.celdas = celdas;
    }

    public Laberinto(String[] mapa) {
        if (
            mapa == null ||
            mapa.length == 0 ||
            mapa[0] == null ||
            mapa[0].isEmpty()
        ) {
            throw new IllegalArgumentException("El mapa no puede estar vacío");
        }

        int columnas = mapa[0].length();
        this.celdas = new Celda[mapa.length][columnas];

        for (int fila = 0; fila < mapa.length; fila++) {
            if (mapa[fila] == null || mapa[fila].length() != columnas) {
                throw new IllegalArgumentException(
                    "Todas las filas del mapa deben tener el mismo largo"
                );
            }
            for (int columna = 0; columna < columnas; columna++) {
                char simbolo = mapa[fila].charAt(columna);
                Celda celda;
                switch (simbolo) {
                    case '#' -> celda = new Celda(true, null, fila, columna);
                    case '.' -> celda = new Celda(
                        false,
                        new PuntoComun(),
                        fila,
                        columna
                    );
                    case 'o' -> celda = new Celda(
                        false,
                        new SuperPunto(),
                        fila,
                        columna
                    );
                    case ' ' -> celda = new Celda(false, null, fila, columna);
                    default -> throw new IllegalArgumentException(
                        "Símbolo no válido '" + simbolo + "' en fila " +
                        fila + ", columna " + columna
                    );
                }
                this.celdas[fila][columna] = celda;
            }
        }
    }

    public static Laberinto crearDefault() {
        return new Laberinto();
    }

    public boolean esTransitable(Posicion posicion)
    {
        Celda celda = obtenerCelda(posicion);
        return celda != null && !celda.esPared();
    }

    public List<Direccion> direccionesDisponibles(Posicion posicion)
    {
        List<Direccion> direcciones = new ArrayList<>();
        if (!esTransitable(posicion)) {
            return direcciones;
        }

        for (Direccion direccion : Direccion.values()) {
            if (esTransitable(vecino(posicion, direccion))) {
                direcciones.add(direccion);
            }
        }
        return direcciones;
    }

    public List<Posicion> caminoMasCorto(
        Posicion origen,
        Posicion destino
    )
    {
        if (!esTransitable(origen) || !esTransitable(destino)) {
            return Collections.emptyList();
        }
        if (mismaPosicion(origen, destino)) {
            return List.of(copiar(origen));
        }

        boolean[][] visitado = new boolean[getFilas()][getColumnas()];
        Posicion[][] padre = new Posicion[getFilas()][getColumnas()];
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();

        visitado[origen.fila][origen.columna] = true;
        pendientes.addLast(copiar(origen));

        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            if (mismaPosicion(actual, destino)) {
                return reconstruirCamino(padre, origen, destino);
            }

            for (Direccion direccion : Direccion.values()) {
                Posicion proxima = vecino(actual, direccion);
                if (
                    !esTransitable(proxima) ||
                    visitado[proxima.fila][proxima.columna]
                ) {
                    continue;
                }

                visitado[proxima.fila][proxima.columna] = true;
                padre[proxima.fila][proxima.columna] = actual;
                pendientes.addLast(proxima);
            }
        }

        return Collections.emptyList();
    }

    public boolean quedanPuntos()
    {
        for (Celda[] fila : celdas) {
            for (Celda celda : fila) {
                if (celda.tienePunto()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Posicion centro()
    {
        return new Posicion(getFilas() / 2, getColumnas() / 2);
    }

    public Celda obtenerCelda(Posicion posicion)
    {
        if (posicion == null) {
            return null;
        }

        int fila = posicion.fila;
        int columna = posicion.columna;
        if (
            fila < 0 ||
            fila >= getFilas() ||
            columna < 0 ||
            columna >= getColumnas()
        ) {
            return null;
        }

        return celdas[fila][columna];
    }

    public int getFilas() {
        return celdas.length;
    }

    public int getColumnas() {
        return celdas[0].length;
    }

    public void imprimirLaberinto() {
        for (Celda[] fila : celdas) {
            for (Celda celda : fila) {
                if (celda.esPared()) {
                    System.out.print("# ");
                } else if (celda.tipoPunto() instanceof SuperPunto) {
                    System.out.print("o ");
                } else if (celda.tienePunto()) {
                    System.out.print(". ");
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Laberinto.crearDefault().imprimirLaberinto();
    }

    private Posicion vecino(Posicion posicion, Direccion direccion) {
        int fila = posicion.fila;
        int columna = posicion.columna;
        switch (direccion) {
            case ARRIBA -> fila -= 1;
            case ABAJO -> fila += 1;
            case IZQUIERDA -> columna -= 1;
            case DERECHA -> columna += 1;
            default -> throw new IllegalArgumentException(
                "Dirección no válida: " + direccion
            );
        }
        return new Posicion(fila, columna);
    }

    private List<Posicion> reconstruirCamino(
        Posicion[][] padre,
        Posicion origen,
        Posicion destino
    ) {
        List<Posicion> camino = new ArrayList<>();
        Posicion actual = destino;

        while (actual != null) {
            camino.add(copiar(actual));
            if (mismaPosicion(actual, origen)) {
                break;
            }
            actual = padre[actual.fila][actual.columna];
        }

        Collections.reverse(camino);
        return camino;
    }

    private boolean mismaPosicion(Posicion primera, Posicion segunda) {
        return primera != null &&
            segunda != null &&
            primera.fila == segunda.fila &&
            primera.columna == segunda.columna;
    }

    private Posicion copiar(Posicion posicion) {
        return new Posicion(posicion.fila, posicion.columna);
    }
}
