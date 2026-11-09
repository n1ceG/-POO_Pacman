package controlador.dto;

import java.util.Collections;
import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) que empaqueta el estado global
 * de la partida para desacoplar completamente la vista del modelo.
 */
public class PartidaDTO {

    private final int puntaje;
    private final int vidas;
    private final String estado;
    private final int pacManFila;
    private final int pacManColumna;
    private final String pacManDireccion;
    private final List<FantasmaDTO> fantasmas;

    public PartidaDTO(
        int puntaje,
        int vidas,
        String estado,
        int pacManFila,
        int pacManColumna,
        String pacManDireccion,
        List<FantasmaDTO> fantasmas
    ) {
        this.puntaje = puntaje;
        this.vidas = vidas;
        this.estado = estado;
        this.pacManFila = pacManFila;
        this.pacManColumna = pacManColumna;
        this.pacManDireccion = pacManDireccion;
        this.fantasmas = fantasmas != null
            ? Collections.unmodifiableList(fantasmas)
            : Collections.emptyList();
    }

    public int getPuntaje() {
        return puntaje;
    }

    public int getVidas() {
        return vidas;
    }

    public String getEstado() {
        return estado;
    }

    public int getPacManFila() {
        return pacManFila;
    }

    public int getPacManColumna() {
        return pacManColumna;
    }

    public String getPacManDireccion() {
        return pacManDireccion;
    }

    public List<FantasmaDTO> getFantasmas() {
        return fantasmas;
    }

    public boolean estaEnCurso() {
        return "EN_CURSO".equalsIgnoreCase(estado);
    }

    public boolean estaPausada() {
        return "PAUSADA".equalsIgnoreCase(estado);
    }

    public boolean esVictoria() {
        return "VICTORIA".equalsIgnoreCase(estado);
    }

    public boolean esDerrota() {
        return "DERROTA".equalsIgnoreCase(estado);
    }

    public boolean estaFinalizada() {
        return esVictoria() || esDerrota();
    }

    public boolean isEnCurso() {
        return estaEnCurso();
    }

    public boolean isPausada() {
        return estaPausada();
    }

    public boolean isVictoria() {
        return esVictoria();
    }

    public boolean isDerrota() {
        return esDerrota();
    }

    public boolean isFinalizada() {
        return estaFinalizada();
    }
}
