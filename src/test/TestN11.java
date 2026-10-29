package test;

import extra.EstadoFantasma;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.Partida;
import modelo.PuntoComun;

/**
 * Pruebas de N11 creadas con asistencia de IA a pedido de Juan.
 * En VS Code: ejecutar Run sobre main(). No requiere JUnit ni -ea.
 * Desde TPO-Pacman, en PowerShell:
 * $fuentesN11 = @(Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object FullName)
 * javac --release 17 -encoding UTF-8 -d bin $fuentesN11
 * Si compila correctamente: java -cp bin test.TestN11
 *
 * Se prueba detectarColision() antes de mover los personajes, con destinos
 * validos, uno por fantasma, en el orden del arreglo de Partida.
 * La reflexion solo prepara escenarios y consulta datos privados para verificar
 * su conservacion; no agrega setters al modelo. No prueba la integracion N15.
 */
public class TestN11 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Llegada a la misma celda", TestN11::mismaLlegada);
        ejecutar("Intercambio horizontal", TestN11::intercambioHorizontal);
        ejecutar("Intercambio vertical", TestN11::intercambioVertical);
        ejecutar("PacMan quieto y fantasma que llega", TestN11::pacManQuieto);
        ejecutar("Fantasma quieto y PacMan que llega", TestN11::fantasmaQuieto);
        ejecutar("Ambos quietos en la misma celda: sin duplicados", TestN11::ambosQuietos);
        ejecutar("Varios contactos de distinto tipo", TestN11::variosContactos);
        ejecutar("Cuatro fantasmas distintos en la misma celda", TestN11::cuatroContactos);
        ejecutar("Sin contacto devuelve una lista vacia", TestN11::sinContacto);
        ejecutar("Una sola mitad del intercambio no alcanza", TestN11::intercambioIncompleto);
        ejecutar("Coincidir solo en fila o columna no alcanza", TestN11::coincidenciaParcial);
        ejecutar("Incluye todos los estados sin resolver consecuencias", TestN11::todosLosEstados);
        System.out.println("\nN11: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N11 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static void mismaLlegada() {
        Escenario e = new Escenario();
        e.ubicar(0, 3, 5);
        e.destinos.set(0, new Posicion(3, 4));
        comprobar(e, new Posicion(3, 4), 0);
    }

    private static void intercambioHorizontal() {
        Escenario e = new Escenario();
        e.ubicar(2, 3, 4);
        e.destinos.set(2, new Posicion(3, 3));
        comprobar(e, new Posicion(3, 4), 2);
    }

    private static void intercambioVertical() {
        Escenario e = new Escenario();
        e.ubicar(1, 2, 3);
        e.destinos.set(1, new Posicion(3, 3));
        comprobar(e, new Posicion(2, 3), 1);
    }

    private static void pacManQuieto() {
        Escenario e = new Escenario();
        e.ubicar(0, 3, 4);
        e.destinos.set(0, new Posicion(3, 3));
        comprobar(e, new Posicion(3, 3), 0);
    }

    private static void fantasmaQuieto() {
        Escenario e = new Escenario();
        e.ubicar(3, 3, 4);
        comprobar(e, new Posicion(3, 4), 3);
    }

    private static void ambosQuietos() {
        Escenario e = new Escenario();
        e.ubicar(0, 3, 3);
        // Se cumplen tanto misma llegada como intercambio: debe aparecer una vez.
        comprobar(e, new Posicion(3, 3), 0);
    }

    private static void variosContactos() {
        Escenario e = new Escenario();
        e.ubicar(0, 3, 5);
        e.destinos.set(0, new Posicion(3, 4));
        e.ubicar(1, 3, 4);
        e.destinos.set(1, new Posicion(3, 3));
        e.ubicar(2, 3, 4);
        comprobar(e, new Posicion(3, 4), 0, 1, 2);
    }

    private static void cuatroContactos() {
        Escenario e = new Escenario();
        for (int i = 0; i < e.fantasmas.length; i++) {
            e.ubicar(i, 3, 5);
            e.destinos.set(i, new Posicion(3, 4));
        }
        comprobar(e, new Posicion(3, 4), 0, 1, 2, 3);
    }

    private static void sinContacto() {
        comprobar(new Escenario(), new Posicion(3, 4));
    }

    private static void intercambioIncompleto() {
        Escenario e = new Escenario();
        // PacMan llega al origen del fantasma, que se aleja hacia otra celda.
        e.ubicar(0, 3, 4);
        e.destinos.set(0, new Posicion(3, 5));
        comprobar(e, new Posicion(3, 4));

        e = new Escenario();
        // El fantasma llega al origen de PacMan, que se aleja hacia otra celda.
        e.ubicar(0, 3, 2);
        e.destinos.set(0, new Posicion(3, 3));
        comprobar(e, new Posicion(3, 4));
    }

    private static void coincidenciaParcial() {
        Escenario e = new Escenario();
        e.ubicar(0, 3, 6); // Misma fila que el destino de PacMan.
        e.ubicar(1, 1, 4); // Misma columna que el destino de PacMan.
        comprobar(e, new Posicion(3, 4));
    }

    private static void todosLosEstados() {
        Escenario e = new Escenario();
        for (int i = 0; i < 3; i++) {
            e.ubicar(i, 3, 4);
        }
        e.fantasmas[1].asustar(7);
        e.fantasmas[2].asustar(10);
        e.fantasmas[2].serComido();
        // Puntaje distinto de cero para detectar un eventual reinicio indebido.
        e.partida.consumir(new PuntoComun());
        verificar("Preparacion: perseguidor", EstadoFantasma.PERSIGUIENDO,
            e.fantasmas[0].getEstado());
        verificar("Preparacion: asustado", EstadoFantasma.ASUSTADO,
            e.fantasmas[1].getEstado());
        verificar("Preparacion: regresando", EstadoFantasma.REGRESANDO,
            e.fantasmas[2].getEstado());
        comprobar(e, new Posicion(3, 4), 0, 1, 2);
    }

    private static void comprobar(Escenario e, Posicion destinoPacMan, int... indices) {
        String partidaAntes = estadoPartida(e);
        List<Posicion> destinosAntes = new ArrayList<>(e.destinos);
        String parametrosAntes = coordenadas(destinoPacMan) + coordenadas(e.destinos);

        List<Fantasma> contactos = e.partida.detectarColision(destinoPacMan, e.destinos);

        verificar("Conserva vidas, puntaje, estado, posiciones y temporizadores",
            partidaAntes, estadoPartida(e));
        verificar("Conserva las coordenadas recibidas", parametrosAntes,
            coordenadas(destinoPacMan) + coordenadas(e.destinos));
        verificar("Conserva el orden y las posiciones de la lista recibida",
            destinosAntes, e.destinos);
        verificar("Devuelve una lista, nunca null", true, contactos != null);
        if (contactos == null) {
            return;
        }
        verificar("Cantidad exacta de contactos", indices.length, contactos.size());
        // Se compara identidad: deben ser los objetos de la partida, sin copias
        // ni duplicados. El contrato no exige un orden para el resultado.
        for (int i = 0; i < e.fantasmas.length; i++) {
            boolean esperado = false;
            for (int indice : indices) {
                if (indice == i) {
                    esperado = true;
                }
            }
            int apariciones = 0;
            for (Fantasma contacto : contactos) {
                if (contacto == e.fantasmas[i]) {
                    apariciones++;
                }
            }
            verificar("Apariciones del fantasma " + i, esperado ? 1 : 0, apariciones);
        }
    }

    private static String estadoPartida(Escenario e) {
        StringBuilder estado = new StringBuilder();
        estado.append(e.partida.getVidas()).append('|')
            .append(e.partida.getPuntaje()).append('|')
            .append(e.partida.getEstado()).append('|')
            .append(coordenadas(e.partida.getPosicionPacMan()));
        for (Fantasma fantasma : e.fantasmas) {
            estado.append('|').append(coordenadas(fantasma.getPosicion()))
                .append('|').append(fantasma.getEstado())
                .append('|').append(fantasma.getDireccion())
                .append('|').append(leerCampo(fantasma, "tiempoSustoRestante"));
        }
        return estado.toString();
    }

    private static String coordenadas(Posicion posicion) {
        return "(" + posicion.fila + "," + posicion.columna + ")";
    }

    private static String coordenadas(List<Posicion> posiciones) {
        StringBuilder resultado = new StringBuilder();
        for (Posicion posicion : posiciones) {
            resultado.append(coordenadas(posicion));
        }
        return resultado.toString();
    }

    private static Object leerCampo(Object objeto, String nombre) {
        try {
            Field campo = objeto.getClass().getDeclaredField(nombre);
            campo.setAccessible(true);
            return campo.get(objeto);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("No se pudo consultar " + nombre, error);
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

    private static class Escenario {
        final Partida partida;
        final Fantasma[] fantasmas;
        final List<Posicion> destinos = new ArrayList<>();

        Escenario() {
            Laberinto laberinto = new Laberinto(new String[] {
                "#########",
                "#.......#",
                "#.......#",
                "#.......#",
                "#.......#",
                "#.......#",
                "#########"
            });
            partida = new Partida(laberinto, new Posicion(3, 3));
            fantasmas = (Fantasma[]) leerCampo(partida, "fantasmas");
            // Fantasmas alejados del paso probado; cada caso ubica los necesarios.
            int[][] origenes = {{1, 1}, {1, 7}, {5, 1}, {5, 7}};
            for (int i = 0; i < fantasmas.length; i++) {
                destinos.add(null);
                ubicar(i, origenes[i][0], origenes[i][1]);
            }
        }

        void ubicar(int indice, int fila, int columna) {
            fantasmas[indice] = new Fantasma(new Posicion(fila, columna));
            destinos.set(indice, new Posicion(fila, columna));
        }
    }
}
