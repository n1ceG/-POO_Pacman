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
        int x = this.posicion.getPosicion()[0];
        int y = this.posicion.getPosicion()[1];
        if(null != direccion)switch (direccion) {
            case ARRIBA:
                y-=1;
                break;
            case ABAJO:
                y+=1;
                break;
            case IZQUIERDA:
                x-=1;
                break;
            case DERECHA:
                x+=1;
                break;
            default:
                break;
        }
        Posicion proximaPosicion = new Posicion(x,y);
        return proximaPosicion;
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
