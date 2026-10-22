package modelo;

import extra.Direccion;
import extra.Posicion;

public abstract class Personaje {

    protected Posicion posicion;
    protected Posicion posicionInicial;
    protected Direccion direccion;

    public Posicion calcularProximaPosicion(
        Direccion direccion,
        Laberinto laberinto
    )
    {
        return null;
    }

    public void mover(
        Direccion direccion,
        Laberinto laberinto
    )
    {}

    public void volverAlInicio()
    {}

    public Posicion getPosicion()
    { return null; }

    public Direccion getDireccion()
    { return null; }

    protected Posicion getPosicionInicial()
    { return null; }
}
