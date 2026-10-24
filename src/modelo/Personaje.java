package modelo;

import extra.Direccion;
import extra.Posicion;

public abstract class Personaje {

    protected Posicion posicion;
    protected Posicion posicionInicial;
    protected Direccion direccion;

    public Posicion calcularProximaPosicion
    (
        Laberinto laberinto
    )
    {
        int fila = this.posicion.fila;
        int columna = this.posicion.columna;
        if(direccion != null) switch (direccion) {
            case ARRIBA -> fila -= 1;
            case ABAJO -> fila +=1;
            case IZQUIERDA -> columna-=1;
            case DERECHA -> columna+=1;
            default -> {
            }
        }
        return new Posicion(fila, columna);
    }

    public abstract void mover(
        Direccion direccion,
        Laberinto laberinto
    );

    public abstract void volverAlInicio();

    public abstract Posicion getPosicion();

    public abstract Direccion getDireccion();

    protected abstract Posicion getPosicionInicial();
}
