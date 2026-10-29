package test;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.Partida;

public class TestN15 {

    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args)
    {
        pruebas = 0;
        aprobadas = 0;

        ejecutar("Superpunto y contacto: primero se consume el superpunto",
            TestN15::superpuntoYContacto);
        ejecutar("Susto por vencer y contacto: primero se resuelve el contacto",
            TestN15::vencimientoYContacto);
        ejecutar("Ultimo punto y contacto: primero se declara VICTORIA",
            TestN15::ultimoPuntoYContacto);
        ejecutar("Dos perseguidores en el mismo paso: una sola vida",
            TestN15::multiplesPerseguidores);
        ejecutar("Ultima vida: se descartan movimientos y posiciones",
            TestN15::ultimaVidaDescartaMovimientos);
        ejecutar("Dos fantasmas asustados: dos premios y ninguna vida",
            TestN15::multiplesAsustados);
        ejecutar("Contacto mixto: una sola vida",
            TestN15::contactoMixto);
        ejecutar("Contacto con fantasma REGRESANDO: inofensivo",
            TestN15::contactoRegresando);
        ejecutar("Cruce con fantasma persiguiendo: una vida",
            TestN15::crucePersiguiendo);
        ejecutar("Cruce con fantasma asustado: premio sin vida",
            TestN15::cruceAsustado);
        ejecutar("Susto nuevo: conserva los 10 segundos",
            TestN15::sustoNuevoDiezSegundos);

        System.out.println("\nN15: " + aprobadas + "/" + pruebas
            + " comprobaciones OK");

        if (aprobadas != pruebas) {
            throw new AssertionError(
                "N15 tiene fallas. Revisar los mensajes [FALLA]."
            );
        }
    }

    // ------------------------------------------------------------------
    // Casos
    // ------------------------------------------------------------------

    private static void superpuntoYContacto()
    {
        Posicion centro = new Posicion(2, 3);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 3, 1),
            fondo(centro, 3, 4),
            fondo(centro, 3, 5)
        };
        Partida partida = partida(salaConSuperpunto(), 1, 1, fantasmas);
        fijarVidas(partida, 1);

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("Puntaje: superpunto (50) + fantasma (200)", 250,
            partida.getPuntaje());
        verificar("Vidas: el superpunto evita la muerte", 1,
            partida.getVidas());
        verificar("Estado", EstadoPartida.EN_CURSO, partida.getEstado());
        verificar("Fantasma 0 comido", EstadoFantasma.REGRESANDO,
            estado(partida, 0));
        verificarPosicion("PacMan avanza al superpunto", 1, 2,
            partida.getPosicionPacMan());
        verificar("Fondo asustado por el superpunto", EstadoFantasma.ASUSTADO,
            estado(partida, 1));
        verificar("Fondo conserva 10 segundos", 10.0, tiempo(fantasmas(partida)[1]));
    }

    private static void vencimientoYContacto()
    {
        Posicion centro = new Posicion(2, 3);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 3, 1),
            fondo(centro, 3, 4),
            fondo(centro, 3, 5)
        };
        Partida partida = partida(salaNormal(), 1, 1, fantasmas);
        fijarVidas(partida, 1);

        // 0.1 s de susto y un paso de 1 s: el contacto debe resolverse
        // antes de que el estado pase a PERSIGUIENDO.
        fantasmas(partida)[0].asustar(0.1);

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("Puntaje: se come al fantasma asustado", 200,
            partida.getPuntaje());
        verificar("Vidas: el contacto no mata", 1, partida.getVidas());
        verificar("Estado", EstadoPartida.EN_CURSO, partida.getEstado());
        verificar("Fantasma comido", EstadoFantasma.REGRESANDO,
            estado(partida, 0));
        verificarPosicion("PacMan", 1, 2, partida.getPosicionPacMan());
    }

    private static void ultimoPuntoYContacto()
    {
        Posicion centro = new Posicion(2, 3);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 2, 1),
            fondo(centro, 2, 4),
            fondo(centro, 2, 5)
        };
        Partida partida = partida(ultimoPunto(), 1, 1, fantasmas);
        fijarVidas(partida, 1);

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("Estado: gana antes del contacto", EstadoPartida.VICTORIA,
            partida.getEstado());
        verificar("Vidas: no se descuenta la ultima", 1, partida.getVidas());
        verificar("Puntaje del ultimo punto", 10, partida.getPuntaje());
        verificarPosicion("PacMan llega al ultimo punto", 1, 2,
            partida.getPosicionPacMan());
        verificarPosicion("El fantasma no se mueve al ganar", 1, 3,
            partida.getPosicionesFantasmas()[0]);
    }

    private static void multiplesPerseguidores()
    {
        Posicion centro = new Posicion(2, 2);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 1, Direccion.DERECHA),
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 3, 1),
            fondo(centro, 3, 3)
        };
        Partida partida = partida(sala5x5(), 2, 2, fantasmas);

        partida.elegirDireccionPacman(Direccion.ARRIBA);
        partida.actualizar(1);

        verificar("Dos contactos descuentan una sola vida", 2,
            partida.getVidas());
        verificar("Estado", EstadoPartida.EN_CURSO, partida.getEstado());
        verificarPosicion("PacMan vuelve al inicio", 2, 2,
            partida.getPosicionPacMan());
        verificarFantasmasEn("Los fantasmas vuelven al centro", partida, 2, 2);
    }

    private static void ultimaVidaDescartaMovimientos()
    {
        Posicion centro = new Posicion(2, 2);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 1, Direccion.DERECHA),
            fondo(centro, 3, 3),
            fondo(centro, 3, 1),
            fondo(centro, 2, 1)
        };
        Partida partida = partida(sala5x5(), 2, 2, fantasmas);
        fijarVidas(partida, 1);

        partida.elegirDireccionPacman(Direccion.ARRIBA);
        partida.actualizar(1);

        // Caso de regresion: al llegar a 0 vidas, perderVida() tambien debe
        // reiniciar PacMan y los fantasmas antes de declarar DERROTA.
        verificar("Vidas al perder la ultima", 0, partida.getVidas());
        verificar("Estado final", EstadoPartida.DERROTA, partida.getEstado());
        verificarPosicion("PacMan vuelve al inicio aunque pierda", 2, 2,
            partida.getPosicionPacMan());
        verificarFantasmasEn("Los fantasmas vuelven al centro aunque pierda",
            partida, 2, 2);
    }

    private static void multiplesAsustados()
    {
        Posicion centro = new Posicion(2, 2);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 1, Direccion.DERECHA),
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 3, 1),
            fondo(centro, 3, 3)
        };
        Partida partida = partida(sala5x5(), 2, 2, fantasmas);
        fijarVidas(partida, 1);

        fantasmas(partida)[0].asustar(10);
        fantasmas(partida)[1].asustar(10);

        partida.elegirDireccionPacman(Direccion.ARRIBA);
        partida.actualizar(1);

        verificar("Dos fantasmas comidos suman 400", 400, partida.getPuntaje());
        verificar("Comer fantasmas no descuenta vidas", 1, partida.getVidas());
        verificar("Estado", EstadoPartida.EN_CURSO, partida.getEstado());
        verificar("Fantasma 0 regresa", EstadoFantasma.REGRESANDO,
            estado(partida, 0));
        verificar("Fantasma 1 regresa", EstadoFantasma.REGRESANDO,
            estado(partida, 1));
    }

    private static void contactoMixto()
    {
        for (int indiceAsustado = 0; indiceAsustado < 2; indiceAsustado++) {
            contactoMixto(indiceAsustado);
        }
    }

    private static void contactoMixto(int indiceAsustado)
    {
        Posicion centro = new Posicion(2, 2);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 1, Direccion.DERECHA),
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 3, 1),
            fondo(centro, 3, 3)
        };
        Partida partida = partida(sala5x5(), 2, 2, fantasmas);
        fijarVidas(partida, 3);

        fantasmas(partida)[indiceAsustado].asustar(10);

        partida.elegirDireccionPacman(Direccion.ARRIBA);
        partida.actualizar(1);

        verificar("Cobra al asustado independientemente del orden", 200,
            partida.getPuntaje());
        verificar("Contacto mixto descuenta una sola vida", 2,
            partida.getVidas());
        verificar("Estado", EstadoPartida.EN_CURSO, partida.getEstado());
        verificarPosicion("PacMan vuelve al inicio", 2, 2,
            partida.getPosicionPacMan());
    }

    private static void contactoRegresando()
    {
        Posicion centro = new Posicion(2, 3);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 3, 1),
            fondo(centro, 3, 4),
            fondo(centro, 3, 5)
        };
        Partida partida = partida(salaNormal(), 1, 1, fantasmas);
        fijarVidas(partida, 1);

        Fantasma fantasma = fantasmas(partida)[0];
        fantasma.asustar(10);
        fantasma.serComido();
        verificar("Preparacion: REGRESANDO", EstadoFantasma.REGRESANDO,
            fantasma.getEstado());

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("Contacto con REGRESANDO no suma", 0,
            partida.getPuntaje());
        verificar("Contacto con REGRESANDO no resta vida", 1,
            partida.getVidas());
        verificar("Sigue REGRESANDO", EstadoFantasma.REGRESANDO,
            estado(partida, 0));
        verificarPosicion("PacMan", 1, 2, partida.getPosicionPacMan());
    }

    private static void crucePersiguiendo()
    {
        Posicion centro = new Posicion(2, 3);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 2, 1),
            fondo(centro, 2, 4),
            fondo(centro, 2, 5)
        };
        Partida partida = partida(salaCruce(), 1, 2, fantasmas);
        fijarVidas(partida, 3);

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("El cruce descuenta una vida", 2, partida.getVidas());
        verificar("Estado", EstadoPartida.EN_CURSO, partida.getEstado());
        verificarPosicion("PacMan vuelve al inicio", 1, 2,
            partida.getPosicionPacMan());
        verificarFantasmasEn("Los fantasmas vuelven al centro", partida, 2, 3);
    }

    private static void cruceAsustado()
    {
        Posicion centro = new Posicion(2, 3);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 3, Direccion.IZQUIERDA),
            fondo(centro, 2, 1),
            fondo(centro, 2, 4),
            fondo(centro, 2, 5)
        };
        Partida partida = partida(salaCruce(), 1, 2, fantasmas);
        fijarVidas(partida, 1);
        fantasmas(partida)[0].asustar(10);

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("El cruce con asustado suma 200", 200, partida.getPuntaje());
        verificar("No descuenta vida", 1, partida.getVidas());
        verificar("Fantasma comido", EstadoFantasma.REGRESANDO,
            estado(partida, 0));
        verificarPosicion("PacMan termina el cruce", 1, 3,
            partida.getPosicionPacMan());
    }

    private static void sustoNuevoDiezSegundos()
    {
        Posicion centro = new Posicion(2, 3);
        FantasmaFijo[] fantasmas = {
            contacto(centro, 1, 3, Direccion.DERECHA),
            fondo(centro, 3, 1),
            fondo(centro, 3, 4),
            fondo(centro, 3, 5)
        };
        Partida partida = partida(salaConSuperpunto(), 1, 1, fantasmas);

        partida.elegirDireccionPacman(Direccion.DERECHA);
        partida.actualizar(1);

        verificar("Puntaje del superpunto", 50, partida.getPuntaje());
        verificar("Todos asustados", true,
            todosAsustados(partida));
        for (int i = 0; i < 4; i++) {
            verificar("Fantasma " + i + " conserva 10 segundos", 10.0,
                tiempo(fantasmas(partida)[i]));
        }
        verificarPosicion("PacMan", 1, 2, partida.getPosicionPacMan());
        verificarPosicion("Fantasma 0 se movio", 1, 4,
            partida.getPosicionesFantasmas()[0]);
    }

    // ------------------------------------------------------------------
    // Preparacion y utilidades
    // ------------------------------------------------------------------

    private static String[] salaNormal()
    {
        return new String[] {
            "#######",
            "#     #",
            "#     #",
            "# .   #",
            "#######"
        };
    }

    private static String[] salaConSuperpunto()
    {
        return new String[] {
            "#######",
            "# o   #",
            "#     #",
            "# .   #",
            "#######"
        };
    }

    private static String[] sala5x5()
    {
        return new String[] {
            "#####",
            "#   #",
            "#   #",
            "# . #",
            "#####"
        };
    }

    private static String[] salaCruce()
    {
        return new String[] {
            "#######",
            "#     #",
            "#  .  #",
            "#######"
        };
    }

    private static String[] ultimoPunto()
    {
        return new String[] {
            "#######",
            "# .   #",
            "#     #",
            "#######"
        };
    }

    private static Partida partida(String[] mapa, int fila, int columna,
            Fantasma... fantasmas)
    {
        Partida partida = new Partida(new Laberinto(mapa),
            new Posicion(fila, columna));
        instalarFantasmas(partida, fantasmas);
        return partida;
    }

    private static FantasmaFijo contacto(Posicion centro, int fila, int columna,
            Direccion direccion)
    {
        return new FantasmaFijo(centro, direccion).ubicado(fila, columna);
    }

    private static FantasmaFijo fondo(Posicion centro, int fila, int columna)
    {
        return new FantasmaFijo(centro, Direccion.ABAJO).ubicado(fila, columna);
    }

    private static void instalarFantasmas(Partida partida, Fantasma... fantasmas)
    {
        try {
            Field campo = Partida.class.getDeclaredField("fantasmas");
            campo.setAccessible(true);
            campo.set(partida, fantasmas);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(
                "No se pudieron preparar los fantasmas", error);
        }
    }

    private static Fantasma[] fantasmas(Partida partida)
    {
        try {
            Field campo = Partida.class.getDeclaredField("fantasmas");
            campo.setAccessible(true);
            return (Fantasma[]) campo.get(partida);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(
                "No se pudieron consultar los fantasmas", error);
        }
    }

    private static void fijarVidas(Partida partida, int vidas)
    {
        try {
            Field campo = Partida.class.getDeclaredField("vidas");
            campo.setAccessible(true);
            campo.setInt(partida, vidas);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(
                "No se pudieron preparar las vidas", error);
        }
    }

    private static EstadoFantasma estado(Partida partida, int indice)
    {
        return partida.getEstadosFantasmas()[indice];
    }

    private static double tiempo(Fantasma fantasma)
    {
        try {
            Field campo = Fantasma.class.getDeclaredField("tiempoSustoRestante");
            campo.setAccessible(true);
            return campo.getDouble(fantasma);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(
                "No se pudo consultar el temporizador del susto", error);
        }
    }

    private static boolean todosAsustados(Partida partida)
    {
        for (EstadoFantasma estado : partida.getEstadosFantasmas()) {
            if (estado != EstadoFantasma.ASUSTADO) {
                return false;
            }
        }
        return true;
    }

    private static void verificarPosicion(String caso, int fila, int columna,
            Posicion actual)
    {
        verificar(caso + ": fila", fila, actual.fila);
        verificar(caso + ": columna", columna, actual.columna);
    }

    private static void verificarFantasmasEn(String caso, Partida partida,
            int fila, int columna)
    {
        Posicion[] posiciones = partida.getPosicionesFantasmas();
        for (int i = 0; i < posiciones.length; i++) {
            verificarPosicion(caso + " - fantasma " + i, fila, columna,
                posiciones[i]);
        }
    }

    private static void verificar(String caso, Object esperado, Object obtenido)
    {
        pruebas++;
        boolean ok = Objects.deepEquals(esperado, obtenido);
        if (ok) {
            aprobadas++;
        }
        System.out.println((ok ? "[OK] " : "[FALLA] ") + caso
            + " | esperado: " + mostrar(esperado)
            + " | obtenido: " + mostrar(obtenido));
    }

    private static String mostrar(Object valor)
    {
        if (valor instanceof Object[]) {
            return Arrays.deepToString((Object[]) valor);
        }
        if (valor instanceof double[]) {
            return Arrays.toString((double[]) valor);
        }
        return String.valueOf(valor);
    }

    private static void ejecutar(String nombre, Runnable caso)
    {
        System.out.println("\n-- " + nombre + " --");
        try {
            caso.run();
        } catch (RuntimeException | AssertionError error) {
            pruebas++;
            System.out.println("[FALLA] " + nombre + ": " + error);
            error.printStackTrace(System.out);
        }
    }

    /**
     * Fantasma con dirección fija. Permite preparar contactos y cruces sin
     * depender del azar de ASUSTADO ni del desempate de caminoMasCorto.
     */
    private static final class FantasmaFijo extends Fantasma {

        private final Direccion direccionFija;

        FantasmaFijo(Posicion centro, Direccion direccionFija)
        {
            super(centro);
            this.direccionFija = direccionFija;
        }

        FantasmaFijo ubicado(int fila, int columna)
        {
            this.posicion = new Posicion(fila, columna);
            return this;
        }

        @Override
        public Direccion elegirDireccion(Laberinto laberinto, Posicion objetivo)
        {
            return this.direccionFija;
        }
    }
}
