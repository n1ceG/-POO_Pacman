package test;

import controlador.ControladorJuego;
import java.lang.reflect.Field;
import modelo.Partida;

/** Prepara escenarios de prueba sin agregar acceso al modelo en la API publica. */
final class SoportePruebas {
    private SoportePruebas() {}

    static ControladorJuego controlador(Partida partida) {
        ControladorJuego controlador = new ControladorJuego();
        try {
            Field campo = ControladorJuego.class.getDeclaredField("partida");
            campo.setAccessible(true);
            campo.set(controlador, partida);
            return controlador;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("No se pudo preparar la partida de prueba", ex);
        }
    }
}
