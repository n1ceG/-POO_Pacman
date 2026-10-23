package modelo;

import extra.Posicion;

public class Celda {

    private Posicion posicion;
    private boolean pared;
    private Punto punto;

    public Celda
    (
        boolean esPared, 
        boolean esSuperPunto, 
        int x, 
        int y
    ){
        this.posicion = new Posicion(x, y);
        if(esPared)
        {
            this.pared = true;
            this.punto = null;
        }
        else
        {
            this.pared = false;
            if(esSuperPunto) this.punto = new SuperPunto();
            else this.punto = new PuntoComun();
        }
    }

    public Punto retirarPunto()
    { return null; }

}
