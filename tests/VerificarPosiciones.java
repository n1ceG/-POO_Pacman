import controlador.ControladorJuego;
import dto.PosicionDTO;
import modelo.*;
import extra.Direccion;
import java.lang.reflect.*;

/** Verificacion de la separacion modelo/DTO, preparada con asistencia de IA. */
public class VerificarPosiciones {
    private static int comprobaciones;
    private static void comprobar(boolean valor, String mensaje) {
        comprobaciones++;
        if (!valor) throw new AssertionError(mensaje);
    }
    public static void main(String[] args) throws Exception {
        comprobar(Personaje.class.getDeclaredField("posicion").getType() == Posicion.class,
            "Personaje debe guardar modelo.Posicion");
        comprobar(Celda.class.getDeclaredField("posicion").getType() == Posicion.class,
            "Celda debe guardar modelo.Posicion");
        comprobar(Partida.class.getMethod("getPosicionPacMan").getReturnType() == Posicion.class,
            "Partida devuelve la posicion interna del modelo");
        comprobar(ControladorJuego.class.getMethod("getPosicionPacMan").getReturnType() == PosicionDTO.class,
            "Controlador devuelve PosicionDTO");
        comprobar(ControladorJuego.class.getMethod("getPosicionesFantasmas").getReturnType() == PosicionDTO[].class,
            "Controlador devuelve DTO de fantasmas");
        for (Method metodo : ControladorJuego.class.getDeclaredMethods()) {
            if (Modifier.isPublic(metodo.getModifiers())) {
                comprobar(!metodo.getGenericReturnType().getTypeName().contains("modelo."),
                    "Consulta publica expone modelo: " + metodo.getName());
            }
        }
        for (Field campo : PosicionDTO.class.getDeclaredFields()) {
            comprobar(Modifier.isPrivate(campo.getModifiers()) && Modifier.isFinal(campo.getModifiers()),
                "DTO con datos privados inmutables");
        }
        ControladorJuego controlador = new ControladorJuego();
        PosicionDTO pacman = controlador.getPosicionPacMan();
        comprobar(pacman.getFila() == 13 && pacman.getColumna() == 7, "Inicio DTO PacMan");
        PosicionDTO[] fantasmas = controlador.getPosicionesFantasmas();
        int[] columnas = {5,6,8,9};
        comprobar(fantasmas.length == 4, "Cuatro DTO de fantasmas");
        for (int i=0;i<4;i++) {
            comprobar(fantasmas[i].getFila() == 7 && fantasmas[i].getColumna() == columnas[i], "Inicio fantasma " + i);
        }
        fantasmas[0] = new PosicionDTO(-1,-1);
        comprobar(controlador.getPosicionesFantasmas()[0].getFila() == 7, "Modificar array DTO no modifica modelo");
        controlador.avanzarTurno();
        comprobar(controlador.getPosicionPacMan().getColumna() == 8, "Movimiento posterior correcto");
        comprobar(pacman.getColumna() == 7, "DTO anterior no cambia al mover modelo");
        comprobar(controlador.getPartidaDTO().getPacManColumna() == 8, "PartidaDTO mantiene coordenadas coherentes");
        comprobar(controlador.getPuntaje() == 10, "Movimiento conserva consumo");
        comprobar(controlador.getTicks() == 1, "Un turno");
        controlador.pausar();
        controlador.avanzarTurno(Direccion.IZQUIERDA);
        comprobar(controlador.getPosicionPacMan().getColumna() == 8, "Pausa conserva posicion");
        PosicionDTO fantasmaDTO = controlador.getPosicionesFantasmas()[0];
        comprobar(fantasmaDTO.getFila() == controlador.getFantasmasDTO().get(0).getFila()
            && fantasmaDTO.getColumna() == controlador.getFantasmasDTO().get(0).getColumna(), "FantasmaDTO coherente");
        PacMan personaje = new PacMan(new Posicion(3,5));
        personaje.getPosicion().fila = 99;
        comprobar(personaje.getPosicion().fila == 3, "Copia de posicion dentro del modelo conservada");
        System.out.println("OK: " + comprobaciones + " comprobaciones de separacion, copias y movimiento.");
    }
}