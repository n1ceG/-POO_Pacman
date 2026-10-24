package modelo;

import extra.Posicion;

public class Celda {

    private Posicion posicion;
    private boolean pared;
    private Punto punto;

    public Celda() {
    }

    public Celda
    (
        boolean esPared, 
        boolean esSuperPunto, 
        int x, 
        int y
    ){
        this(
            esPared,
            esPared ? null : (esSuperPunto ? new SuperPunto() : new PuntoComun()),
            x,
            y
        );
    }

    public Celda
    (
        boolean esPared,
        Punto punto,
        int x,
        int y
    ){
        this.posicion = new Posicion(x, y);
        this.pared = esPared;
        this.punto = esPared ? null : punto;
    }

    public Punto retirarPunto()
    {
        Punto puntoRetirado = this.punto;
        this.punto = null;
        return puntoRetirado;
    }

    public Posicion getPosicion(){
        return this.posicion;
    }

    public Punto tipoPunto(){
        return this.punto;
    }

    public boolean esPared(){
        return this.pared;
    }

    public boolean tienePunto(){
        return this.punto != null;
    }

}
