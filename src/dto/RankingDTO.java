package dto;

public final class RankingDTO {

    private final String nombre;
    private final int puntaje;

    public RankingDTO(String nombre, int puntaje) {
        this.nombre = nombre;
        this.puntaje = puntaje;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntaje() {
        return puntaje;
    }
}
