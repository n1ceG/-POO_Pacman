package controlador.dto;

/**
 * Objeto de transferencia de datos (DTO) que representa el estado visual
 * de un fantasma para la capa de presentacion, sin exponer entidades ni enums del modelo.
 */
public class FantasmaDTO {

    private final int fila;
    private final int columna;
    private final String estado; // "PERSIGUIENDO", "ASUSTADO", "REGRESANDO"

    public FantasmaDTO(int fila, int columna, String estado) {
        this.fila = fila;
        this.columna = columna;
        this.estado = estado;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public String getEstado() {
        return estado;
    }

    public boolean estaAsustado() {
        return "ASUSTADO".equalsIgnoreCase(estado);
    }

    public boolean estaRegresando() {
        return "REGRESANDO".equalsIgnoreCase(estado);
    }
}
