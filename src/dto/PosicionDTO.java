package dto;

public final class PosicionDTO {

    private final int fila;
    private final int columna;

    public PosicionDTO(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }
}
