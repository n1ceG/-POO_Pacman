package modelo;

import extra.Direccion;
import extra.Posicion;

public abstract class Personaje {

    protected Posicion posicion;
    protected Posicion posicionInicial;
    protected Direccion direccion;

    protected Personaje(Posicion posicionInicial){
        this.posicionInicial = new Posicion(posicionInicial.fila, posicionInicial.columna);
        this.posicion = new Posicion(posicionInicial.fila, posicionInicial.columna);
    }
    
    public Posicion calcularProximaPosicion
    (
        Direccion direccion,
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
        Posicion candidata = new Posicion(fila, columna);
        if (laberinto.esTransitable(candidata)) {
            return candidata;
        }
        return new Posicion(this.posicion.fila, this.posicion.columna);
    }

    public abstract void mover(
        Direccion direccion,
        Laberinto laberinto
    );

    public abstract void volverAlInicio();

    public Posicion getPosicion(){
        return new Posicion(posicion.fila, posicion.columna);
    }

    public Direccion getDireccion(){
        return direccion;
    }

    protected Posicion getPosicionInicial(){
        return new Posicion(posicionInicial.fila, posicionInicial.columna);
    }


}