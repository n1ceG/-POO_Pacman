package test;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Laberinto;

/**
 * Pruebas de N08 preparadas con asistencia de IA a pedido de Juan.
 * En VS Code: ejecutar Run sobre main(). No requiere JUnit ni -ea.
 * Desde TPO-Pacman, en PowerShell:
 * $fuentesN08 = @(Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object FullName)
 * javac --release 17 -encoding UTF-8 -d bin $fuentesN08
 * Si compila correctamente: java -cp bin test.TestN08
 *
 * Comprueba el estado individual del fantasma y el recorrido de regreso.
 * La conexion con el contacto de PacMan corresponde a N13/N14 y la llamada
 * desde Partida a N15. La reflexion permite consultar el temporizador sin
 * agregar getters al modelo.
 */
public class TestN08 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("serComido deja REGRESANDO y limpia el temporizador", TestN08::serComido);
        ejecutar("La guarda de serComido respeta el estado", TestN08::serComidoSinSusto);
        ejecutar("Regreso por camino transitable hasta el origen", TestN08::regreso);
        ejecutar("Un susto no interrumpe el regreso", TestN08::sustoDuranteRegreso);
        ejecutar("Regreso al centro de la partida", TestN08::regresoAlCentro);
        System.out.println("\nN08: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N08 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static Fantasma nuevoFantasma() {
        return new Fantasma(new Posicion(1, 1));
    }

    private static Laberinto mapaDeRegreso() {
        return new Laberinto(new String[] {
            "#######",
            "#.....#",
            "#.###.#",
            "#.....#",
            "#######"
        });
    }

    private static void serComido() {
        Fantasma fantasma = nuevoFantasma();
        fantasma.asustar(10);
        fantasma.actualizarEstado(4);
        comprobar("Antes de ser comido", fantasma, EstadoFantasma.ASUSTADO, 6);

        fantasma.serComido();
        comprobar("Tras ser comido", fantasma, EstadoFantasma.REGRESANDO, 0);

        fantasma.serComido();
        comprobar("Segundo serComido no altera el regreso", fantasma, EstadoFantasma.REGRESANDO, 0);

        Fantasma enOrigen = nuevoFantasma();
        enOrigen.asustar(10);
        enOrigen.serComido();
        enOrigen.actualizarEstado(0);
        comprobar("Comido en el origen vuelve a perseguir", enOrigen, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void serComidoSinSusto() {
        Fantasma fantasma = nuevoFantasma();
        fantasma.serComido();
        comprobar("Un perseguidor no pasa a REGRESANDO", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void regreso() {
        Laberinto mapa = mapaDeRegreso();
        Posicion origen = new Posicion(1, 1);
        Fantasma fantasma = new Fantasma(origen);

        mover(fantasma, mapa, Direccion.DERECHA, 4);
        mover(fantasma, mapa, Direccion.ABAJO, 2);
        comprobarPosicion("Fantasma alejado del origen", fantasma, 3, 5);

        fantasma.asustar(10);
        fantasma.serComido();

        int pasos = 0;
        while (!enOrigen(fantasma, origen) && pasos < 20) {
            Direccion direccion = fantasma.elegirDireccion(mapa, new Posicion(1, 5));
            verificar("Paso " + (pasos + 1) + " elige una direccion", direccion != null, true);
            if (direccion == null) {
                break;
            }

            Posicion anterior = fantasma.getPosicion();
            fantasma.mover(direccion, mapa);
            Posicion actual = fantasma.getPosicion();

            verificar("Paso " + (pasos + 1) + " avanza una celda",
                distancia(anterior, actual), 1);
            verificar("Paso " + (pasos + 1) + " no entra en pared",
                mapa.esTransitable(actual), true);

            fantasma.actualizarEstado(0);
            pasos++;
        }

        verificar("Llega al origen", enOrigen(fantasma, origen), true);
        verificar("Recorrido de seis pasos", pasos, 6);
        comprobar("Al llegar al origen", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void sustoDuranteRegreso() {
        Laberinto mapa = mapaDeRegreso();
        Posicion origen = new Posicion(1, 1);
        Fantasma fantasma = new Fantasma(origen);

        mover(fantasma, mapa, Direccion.DERECHA, 4);
        mover(fantasma, mapa, Direccion.ABAJO, 2);

        fantasma.asustar(10);
        fantasma.actualizarEstado(3);
        comprobar("Susto antes de ser comido", fantasma, EstadoFantasma.ASUSTADO, 7);

        fantasma.serComido();
        comprobar("Tras ser comido", fantasma, EstadoFantasma.REGRESANDO, 0);

        fantasma.asustar(10);
        comprobar("Un susto no interrumpe el regreso", fantasma, EstadoFantasma.REGRESANDO, 0);

        fantasma.actualizarEstado(5);
        comprobar("Actualizar durante el regreso no descuenta susto", fantasma, EstadoFantasma.REGRESANDO, 0);

        int pasos = 0;
        while (!enOrigen(fantasma, origen) && pasos < 20) {
            Direccion direccion = fantasma.elegirDireccion(mapa, new Posicion(3, 5));
            verificar("Regreso tras susto: paso " + (pasos + 1), direccion != null, true);
            if (direccion == null) {
                break;
            }
            fantasma.mover(direccion, mapa);
            fantasma.actualizarEstado(1);
            pasos++;
        }

        comprobar("Llega al origen tras el susto", fantasma, EstadoFantasma.PERSIGUIENDO, 0);

        fantasma.actualizarEstado(10);
        comprobar("El temporizador viejo no se reactiva", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void regresoAlCentro() {
        Laberinto mapa = Laberinto.crearDefault();
        Posicion centro = mapa.centro();
        Fantasma fantasma = new Fantasma(centro);

        mover(fantasma, mapa, Direccion.IZQUIERDA, 2);
        comprobarPosicion("Fantasma alejado del centro", fantasma, 7, 5);

        fantasma.asustar(10);
        fantasma.serComido();

        int pasos = 0;
        while (!enOrigen(fantasma, centro) && pasos < 20) {
            Direccion direccion = fantasma.elegirDireccion(mapa, new Posicion(13, 7));
            verificar("Regreso al centro: paso " + (pasos + 1), direccion != null, true);
            if (direccion == null) {
                break;
            }
            fantasma.mover(direccion, mapa);
            fantasma.actualizarEstado(0);
            pasos++;
        }

        comprobarPosicion("Llega al centro", fantasma, 7, 7);
        verificar("Recorrido al centro de dos pasos", pasos, 2);
        comprobar("En el centro vuelve a perseguir", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void mover(Fantasma fantasma, Laberinto mapa, Direccion direccion, int veces) {
        for (int i = 0; i < veces; i++) {
            fantasma.mover(direccion, mapa);
        }
    }

    private static boolean enOrigen(Fantasma fantasma, Posicion origen) {
        Posicion actual = fantasma.getPosicion();
        return actual.fila == origen.fila && actual.columna == origen.columna;
    }

    private static int distancia(Posicion primera, Posicion segunda) {
        return Math.abs(primera.fila - segunda.fila)
            + Math.abs(primera.columna - segunda.columna);
    }

    private static void comprobarPosicion(String descripcion, Fantasma fantasma,
            int fila, int columna) {
        Posicion actual = fantasma.getPosicion();
        verificar(descripcion,
            "(" + fila + "," + columna + ")",
            "(" + actual.fila + "," + actual.columna + ")");
    }

    private static void comprobar(String descripcion, Fantasma fantasma,
            EstadoFantasma estadoEsperado, double tiempoEsperado) {
        verificar(descripcion + " - estado", estadoEsperado, fantasma.getEstado());
        verificar(descripcion + " - segundos restantes", tiempoEsperado, leerTiempo(fantasma));
    }

    private static double leerTiempo(Fantasma fantasma) {
        try {
            Field campo = Fantasma.class.getDeclaredField("tiempoSustoRestante");
            campo.setAccessible(true);
            return campo.getDouble(fantasma);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo consultar el contador", error);
        }
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        pruebas++;
        boolean coincide = Objects.equals(esperado, obtenido);
        if (coincide) {
            aprobadas++;
        }
        System.out.println((coincide ? "[OK] " : "[FALLA] ") + descripcion
            + " | esperado: " + esperado + " | obtenido: " + obtenido);
    }

    private static void ejecutar(String nombre, Runnable caso) {
        System.out.println("\n-- " + nombre + " --");
        try {
            caso.run();
        } catch (RuntimeException | AssertionError error) {
            pruebas++;
            System.out.println("[FALLA] Excepcion en el caso: " + error);
        }
    }
}
