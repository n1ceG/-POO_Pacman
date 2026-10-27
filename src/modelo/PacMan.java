package modelo;

import extra.Direccion;
import extra.Posicion;

public class PacMan extends Personaje {


    public PacMan(Posicion posicionInicial)
    {
        super(posicionInicial);
        this.direccion = Direccion.DERECHA;
    }

    public void elegirDireccion(Direccion direccion)
    {
        this.direccion = direccion;
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
}
