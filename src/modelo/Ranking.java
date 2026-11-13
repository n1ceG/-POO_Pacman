package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ranking {

    private final List<EntradaRanking> entradas = new ArrayList<>();

    public void agregar(String nombre, int puntaje) {
        entradas.add(new EntradaRanking(nombre, puntaje));
        Collections.sort(entradas);
    }

    public List<EntradaRanking> getEntradas() {
        return Collections.unmodifiableList(entradas);
    }
}
