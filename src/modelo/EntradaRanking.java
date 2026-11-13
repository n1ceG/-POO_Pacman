package modelo;

public class EntradaRanking implements Comparable<EntradaRanking> {

    private final String nombre;
    private final int puntaje;

    public EntradaRanking(String nombre, int puntaje) {
        this.nombre = (nombre == null || nombre.isBlank()) ? "Jugador" : nombre.trim();
        this.puntaje = puntaje;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntaje() {
        return puntaje;
    }

    @Override
    public int compareTo(EntradaRanking otra) {
        return Integer.compare(otra.puntaje, this.puntaje);
    }
}
