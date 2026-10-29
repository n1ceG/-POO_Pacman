package test;

import extra.EstadoFantasma;
import extra.Posicion;
import java.lang.reflect.Field;
import java.util.Objects;
import modelo.Fantasma;

/**
 * Pruebas de N06 preparadas con asistencia de IA a pedido de Juan.
 * En VS Code: ejecutar Run sobre main(). No requiere JUnit ni -ea.
 * Desde TPO-Pacman, en PowerShell:
 * $fuentesN06 = @(Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object FullName)
 * javac --release 17 -encoding UTF-8 -d bin $fuentesN06
 * Si compila correctamente: java -cp bin test.TestN06
 *
 * Prueba el estado individual del fantasma; la conexion con el superpunto
 * y la llamada desde Partida corresponden a N09/N15.
 * La reflexion permite consultar el contador sin agregar getters al modelo
 * y preparar REGRESANDO mientras serComido() esta pendiente en N08.
 * Preparar ese estado NO demuestra que serComido() funcione.
 * Se usan duraciones positivas y tiempos transcurridos finitos no negativos.
 */
public class TestN06 {
    private static int pruebas;
    private static int aprobadas;

    public static void main(String[] args) {
        pruebas = 0;
        aprobadas = 0;
        ejecutar("Activacion y descuento hasta el vencimiento exacto", TestN06::descuento);
        ejecutar("Actualizacion que supera el tiempo restante", TestN06::sobrepaso);
        ejecutar("Otro susto reinicia la duracion sin acumular", TestN06::renovacion);
        ejecutar("Respeta la duracion recibida", TestN06::duracionRecibida);
        ejecutar("Cero segundos transcurridos conserva el susto", TestN06::sinTiempoTranscurrido);
        ejecutar("Actualizar un perseguidor no activa susto", TestN06::persecucion);
        ejecutar("Un fantasma regresando ignora el susto", TestN06::regreso);
        ejecutar("Cada fantasma tiene su propio contador", TestN06::independencia);
        System.out.println("\nN06: " + aprobadas + "/" + pruebas + " comprobaciones OK");
        if (aprobadas != pruebas) {
            throw new AssertionError("N06 tiene fallas. Revisar los mensajes [FALLA].");
        }
    }

    private static Fantasma nuevoFantasma() {
        return new Fantasma(new Posicion(1, 1));
    }

    private static void descuento() {
        Fantasma fantasma = nuevoFantasma();
        verificar("Inicialmente persigue", EstadoFantasma.PERSIGUIENDO, fantasma.getEstado());
        fantasma.asustar(10);
        comprobar("Al activar", fantasma, EstadoFantasma.ASUSTADO, 10);
        fantasma.actualizarEstado(3);
        comprobar("Tras 3 segundos", fantasma, EstadoFantasma.ASUSTADO, 7);
        fantasma.actualizarEstado(6.5);
        comprobar("Tras otros 6.5 segundos", fantasma, EstadoFantasma.ASUSTADO, 0.5);
        fantasma.actualizarEstado(0.5);
        comprobar("Al cumplirse 10 segundos", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
        fantasma.actualizarEstado(2);
        comprobar("Despues del vencimiento", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void sobrepaso() {
        Fantasma fantasma = nuevoFantasma();
        fantasma.asustar(10);
        fantasma.actualizarEstado(12);
        comprobar("No deja contador negativo", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void renovacion() {
        Fantasma fantasma = nuevoFantasma();
        fantasma.asustar(10);
        fantasma.actualizarEstado(3);
        fantasma.asustar(10);
        comprobar("Renovado desde 7 segundos", fantasma, EstadoFantasma.ASUSTADO, 10);
        fantasma.actualizarEstado(9.5);
        comprobar("Sigue asustado tras 9.5 desde la renovacion", fantasma, EstadoFantasma.ASUSTADO, 0.5);
        fantasma.actualizarEstado(0.5);
        comprobar("Vence a los 10 desde la renovacion", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void duracionRecibida() {
        Fantasma fantasma = nuevoFantasma();
        // N09 elegira 10; Fantasma debe respetar el parametro recibido.
        fantasma.asustar(2.5);
        comprobar("Duracion personalizada", fantasma, EstadoFantasma.ASUSTADO, 2.5);
        fantasma.actualizarEstado(2.5);
        comprobar("Vence esa duracion", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void sinTiempoTranscurrido() {
        Fantasma fantasma = nuevoFantasma();
        fantasma.asustar(10);
        fantasma.actualizarEstado(0);
        comprobar("Sin tiempo transcurrido", fantasma, EstadoFantasma.ASUSTADO, 10);
    }

    private static void persecucion() {
        Fantasma fantasma = nuevoFantasma();
        fantasma.actualizarEstado(12);
        comprobar("Sigue persiguiendo", fantasma, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void regreso() {
        Fantasma fantasma = nuevoFantasma();
        // Preparacion exclusiva del test: N08 implementara esta transicion.
        try {
            Field campo = Fantasma.class.getDeclaredField("estado");
            campo.setAccessible(true);
            campo.set(fantasma, EstadoFantasma.REGRESANDO);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo preparar REGRESANDO", e);
        }
        fantasma.asustar(10);
        comprobar("Ignora el susto al regresar", fantasma, EstadoFantasma.REGRESANDO, 0);
    }

    private static void independencia() {
        Fantasma primero = nuevoFantasma();
        Fantasma segundo = nuevoFantasma();
        primero.asustar(10);
        segundo.asustar(10);
        primero.actualizarEstado(4);
        segundo.actualizarEstado(10);
        comprobar("El primero conserva su susto", primero, EstadoFantasma.ASUSTADO, 6);
        comprobar("El segundo ya termino", segundo, EstadoFantasma.PERSIGUIENDO, 0);
    }

    private static void comprobar(String descripcion, Fantasma fantasma,
            EstadoFantasma estadoEsperado, double tiempoEsperado) {
        verificar(descripcion + " - estado", estadoEsperado, fantasma.getEstado());
        // Los intervalos usados son representables exactamente en double.
        verificar(descripcion + " - segundos restantes", tiempoEsperado, leerTiempo(fantasma));
    }

    private static double leerTiempo(Fantasma fantasma) {
        try {
            Field campo = Fantasma.class.getDeclaredField("tiempoSustoRestante");
            campo.setAccessible(true);
            return campo.getDouble(fantasma);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo consultar el contador", e);
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
        } catch (RuntimeException | AssertionError e) {
            pruebas++;
            System.out.println("[FALLA] Excepcion en el caso: " + e);
        }
    }
}
