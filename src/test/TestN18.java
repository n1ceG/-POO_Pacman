package test;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Objects;
import modelo.Celda;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.PacMan;
import modelo.Partida;

/**
 * Pruebas N18 preparadas con asistencia de IA. Sin JUnit ni -ea.
 * Compilar src con javac --release 17 -encoding UTF-8 -d bin.
 * Ejecutar: java -cp bin test.TestN18
 * Reinicios y avances pasan por ControladorJuego. Reflection solo consulta
 * objetos internos; no fuerza estados finales ni modifica la implementacion.
 * Se verifica el comportamiento actual: reiniciar usa el mapa predeterminado,
 * incluso si la partida anterior fue construida con un mapa personalizado.
 */
public class TestN18 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Reinicio despues de victoria", TestN18::victoria);
        ejecutar("Reinicio despues de derrota con mapa personalizado", TestN18::derrota);
        ejecutar("No reemplaza partidas en curso o pausadas", TestN18::sinFinal);
        ejecutar("Reinicios sucesivos", TestN18::reiniciosSucesivos);
        System.out.println("\nN18: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (pruebas != aprobadas) {
            throw new AssertionError("N18 tiene fallas. Revisar [FALLA].");
        }
    }

    private static void victoria() {
        Partida anterior = new Partida();
        ControladorJuego c = new ControladorJuego(anterior);
        Laberinto mapaAnterior = mapa(anterior);
        // Preparar un ultimo punto real; actualizar debe consumirlo y ganar.
        vaciarExcepto(mapaAnterior, new Posicion(13, 8));
        c.actualizar(0.25);
        verificar("Preparacion: victoria jugando", EstadoPartida.VICTORIA, c.getEstado());
        verificar("Preparacion: puntaje previo", 10, c.getPuntaje());
        verificar("Preparacion: mapa agotado", false, mapaAnterior.quedanPuntos());
        // Estados anteriores distintos no deben sobrevivir al reemplazo.
        fantasmas(anterior)[0].asustar(7);
        fantasmas(anterior)[1].asustar(8);
        fantasmas(anterior)[1].serComido();
        c.nuevaPartida();
        comprobarNueva(c, anterior);
        verificar("La anterior conserva VICTORIA", EstadoPartida.VICTORIA, anterior.getEstado());
        verificar("La anterior conserva su puntaje", 10, anterior.getPuntaje());
        verificar("La anterior sigue sin puntos", false, mapaAnterior.quedanPuntos());
        comprobarIndependencia(c, anterior);
    }

    private static void derrota() {
        Laberinto mapa = new Laberinto(new String[] {
            "#####", "#...#", "#...#", "#...#", "#####"
        });
        Partida anterior = new Partida(mapa, new Posicion(2, 2));
        ControladorJuego c = new ControladorJuego(anterior);
        // PacMan y fantasmas empiezan en el centro. Quieto, pierde una vida
        // por actualizacion por contacto con perseguidores, hasta DERROTA.
        for (int perdida = 1; perdida <= 3; perdida++) {
            c.moverPacMan(null);
            c.actualizar(0.25);
            verificar("Preparacion: vidas tras contacto " + perdida,
                3 - perdida, c.getVidas());
        }
        verificar("Preparacion: derrota jugando", EstadoPartida.DERROTA, c.getEstado());
        verificar("Preparacion: puntaje previo", 10, c.getPuntaje());
        verificar("Preparacion: punto central consumido", false,
            mapa.obtenerCelda(new Posicion(2, 2)).tienePunto());
        c.nuevaPartida();
        comprobarNueva(c, anterior);
        verificar("La anterior conserva DERROTA", EstadoPartida.DERROTA, anterior.getEstado());
        verificar("La anterior conserva cero vidas", 0, anterior.getVidas());
        comprobarIndependencia(c, anterior);
    }

    private static void sinFinal() {
        Partida original = new Partida();
        ControladorJuego c = new ControladorJuego(original);
        c.actualizar(0.25);
        for (boolean pausada : new boolean[] {false, true}) {
            if (pausada) {
                c.pausar();
            }
            EstadoPartida estado = c.getEstado();
            String posicion = posicion(c.getPosicionPacMan());
            int puntaje = c.getPuntaje();
            int vidas = c.getVidas();
            String contenido = contenido(mapa(original));
            c.nuevaPartida();
            verificar("Sin final: misma partida en " + estado, true, actual(c) == original);
            verificar("Sin final: estado", estado, c.getEstado());
            verificar("Sin final: posicion", posicion, posicion(c.getPosicionPacMan()));
            verificar("Sin final: puntaje", puntaje, c.getPuntaje());
            verificar("Sin final: vidas", vidas, c.getVidas());
            verificar("Sin final: mapa", contenido, contenido(mapa(actual(c))));
        }
    }

    private static void reiniciosSucesivos() {
        ControladorJuego c = new ControladorJuego();
        for (int ciclo = 1; ciclo <= 3; ciclo++) {
            Partida anterior = actual(c);
            vaciarExcepto(mapa(anterior), new Posicion(13, 8));
            c.actualizar(0.25);
            verificar("Ciclo " + ciclo + ": victoria", EstadoPartida.VICTORIA, c.getEstado());
            c.nuevaPartida();
            comprobarNueva(c, anterior);
            Partida recienCreada = actual(c);
            c.nuevaPartida();
            verificar("Ciclo " + ciclo + ": segunda orden no reemplaza", true,
                actual(c) == recienCreada);
        }
    }

    private static void comprobarNueva(ControladorJuego c, Partida anterior) {
        Partida nueva = actual(c);
        verificar("Partida distinta", true, nueva != anterior);
        verificar("Tres vidas", 3, c.getVidas());
        verificar("Puntaje cero", 0, c.getPuntaje());
        verificar("En curso", EstadoPartida.EN_CURSO, c.getEstado());
        verificar("PacMan en su inicio", "(13,7)", posicion(c.getPosicionPacMan()));
        verificar("PacMan hacia la derecha", Direccion.DERECHA, pacman(nueva).getDireccion());
        verificar("PacMan nuevo", true, pacman(nueva) != pacman(anterior));
        verificar("Laberinto nuevo", true, mapa(nueva) != mapa(anterior));
        verificar("Mapa predeterminado completo", contenido(Laberinto.crearDefault()),
            contenido(mapa(nueva)));
        Fantasma[] nuevos = fantasmas(nueva);
        Fantasma[] viejos = fantasmas(anterior);
        verificar("Arreglo nuevo", true, nuevos != viejos);
        verificar("Cuatro fantasmas", 4, nuevos.length);
        for (int i = 0; i < nuevos.length; i++) {
            verificar("Fantasma " + i + ": centro", posicion(mapa(nueva).centro()),
                posicion(c.getPosicionesFantasmas()[i]));
            verificar("Fantasma " + i + ": persiguiendo", EstadoFantasma.PERSIGUIENDO,
                c.getEstadosFantasmas()[i]);
            verificar("Fantasma " + i + ": sin susto", 0.0,
                leer(nuevos[i], "tiempoSustoRestante"));
            boolean distinto = true;
            for (Fantasma viejo : viejos) {
                distinto &= nuevos[i] != viejo;
            }
            verificar("Fantasma " + i + ": objeto nuevo", true, distinto);
        }
    }

    private static void comprobarIndependencia(ControladorJuego c, Partida anterior) {
        Partida nueva = actual(c);
        String mapaNuevo = contenido(mapa(nueva));
        anterior.elegirDireccionPacman(Direccion.IZQUIERDA);
        fantasmas(anterior)[0].asustar(99);
        vaciarExcepto(mapa(anterior), null);
        verificar("Cambiar mapa anterior no cambia nuevo", mapaNuevo, contenido(mapa(nueva)));
        verificar("Cambiar direccion anterior no cambia nueva", Direccion.DERECHA,
            pacman(nueva).getDireccion());
        verificar("Asustar anterior no cambia nuevo", EstadoFantasma.PERSIGUIENDO,
            c.getEstadosFantasmas()[0]);
        String mapaViejo = contenido(mapa(anterior));
        c.actualizar(0.25);
        verificar("Nueva partida avanza hacia derecha", "(13,8)", posicion(c.getPosicionPacMan()));
        verificar("Nueva partida consume un punto", 10, c.getPuntaje());
        verificar("Avanzar nueva no altera mapa anterior", mapaViejo, contenido(mapa(anterior)));
        verificar("Avanzar nueva no cambia direccion anterior", Direccion.IZQUIERDA,
            pacman(anterior).getDireccion());
    }

    private static void vaciarExcepto(Laberinto mapa, Posicion conservar) {
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int col = 0; col < mapa.getColumnas(); col++) {
                if (conservar == null || f != conservar.fila || col != conservar.columna) {
                    mapa.obtenerCelda(new Posicion(f, col)).retirarPunto();
                }
            }
        }
    }

    private static String contenido(Laberinto mapa) {
        StringBuilder resultado = new StringBuilder();
        for (int f = 0; f < mapa.getFilas(); f++) {
            for (int col = 0; col < mapa.getColumnas(); col++) {
                Celda celda = mapa.obtenerCelda(new Posicion(f, col));
                resultado.append(celda.esPared() ? "#" :
                    celda.tienePunto() ? celda.tipoPunto().getClass().getSimpleName() : " ");
                resultado.append('|');
            }
            resultado.append('\n');
        }
        return resultado.toString();
    }

    private static Object leer(Object objeto, String atributo) {
        try {
            Field campo = objeto.getClass().getDeclaredField(atributo);
            campo.setAccessible(true);
            return campo.get(objeto);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo consultar " + atributo, e);
        }
    }

    private static Partida actual(ControladorJuego c) { return (Partida) leer(c, "partida"); }
    private static Laberinto mapa(Partida p) { return (Laberinto) leer(p, "laberinto"); }
    private static PacMan pacman(Partida p) { return (PacMan) leer(p, "pacMan"); }
    private static Fantasma[] fantasmas(Partida p) { return (Fantasma[]) leer(p, "fantasmas"); }
    private static String posicion(Posicion p) { return "(" + p.fila + "," + p.columna + ")"; }

    private static void verificar(String nombre, Object esperado, Object obtenido) {
        pruebas++;
        boolean ok = Objects.equals(esperado, obtenido);
        if (ok) { aprobadas++; }
        System.out.println((ok ? "[OK] " : "[FALLA] ") + nombre
            + " | esperado=" + esperado + " | obtenido=" + obtenido);
    }

    private static void ejecutar(String nombre, Runnable caso) {
        System.out.println("\n-- " + nombre + " --");
        try {
            caso.run();
        } catch (RuntimeException | AssertionError e) {
            pruebas++;
            System.out.println("[FALLA] Caso interrumpido: " + e);
            e.printStackTrace(System.out);
        }
    }
}
