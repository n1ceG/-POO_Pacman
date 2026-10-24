package modelo;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.Posicion;

public class Fantasma extends Personaje {

    private EstadoFantasma estado;
    private double tiempoSustoRestante;

    public Fantasma(Posicion posicionInicial)
    {
        super(posicionInicial);
    }

    @Override
    public void mover(Direccion direccion, Laberinto laberinto)
    {
        this.posicion = calcularProximaPosicion(direccion, laberinto);
    }

    @Override
    public void volverAlInicio()
    {
        this.posicion = getPosicionInicial();
    }

    public Direccion elegirDireccion(
        Laberinto laberinto,
        Posicion objetivo
    )
    {
        return null;
    }

    public void asustar(double duracion)
    {}

    public void serComido()
    {}

    public void actualizarEstado(double tiempoTranscurrido)
    {}

    public EstadoFantasma getEstado()
    { return null; }
}
