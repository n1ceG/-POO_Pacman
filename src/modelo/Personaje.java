package modelo;

import extra.Direccion;
import extra.Posicion;

public abstract class Personaje {

    protected Posicion posicion;
    protected Posicion posicionInicial;
    protected Direccion direccion;

    protected Personaje(Posicion posicionInicial) {
        this.posicionInicial = new Posicion(posicionInicial.fila, posicionInicial.columna);
        this.posicion = new Posicion(posicionInicial.fila, posicionInicial.columna);
    }

    /**
     * Calcula la posicion de la celda adyacente segun la direccion indicada.
     * No valida transitabilidad ya que esa responsabilidad corresponde al laberinto/partida.
     */
    public Posicion calcularProximaPosicion(Direccion direccion) {
        if (direccion == null) {
            return new Posicion(this.posicion.fila, this.posicion.columna);
        }
        int fila = this.posicion.fila;
        int columna = this.posicion.columna;
        switch (direccion) {
            case ARRIBA -> fila -= 1;
            case ABAJO -> fila += 1;
            case IZQUIERDA -> columna -= 1;
            case DERECHA -> columna += 1;
        }
        return new Posicion(fila, columna);
    }

    /**
     * Mueve el personaje en la direccion indicada solo si la celda adyacente es accesible.
     * Desacopla el personaje de la clase Laberinto.
     */
    public void mover(Direccion direccion, boolean puedeAvanzar) {
        if (direccion != null) {
            this.direccion = direccion;
        }
        if (puedeAvanzar && direccion != null) {
            this.posicion = calcularProximaPosicion(direccion);
        }
    }

    /**
     * Mueve el personaje directamente a una posicion destino validada.
     */
    public void moverA(Posicion nuevaPosicion) {
        if (nuevaPosicion != null) {
            this.posicion = new Posicion(nuevaPosicion.fila, nuevaPosicion.columna);
        }
    }

    /**
     * Metodo sobrecargado por compatibilidad: delega la consulta de transitabilidad
     * de la celda adyacente al laberinto y ejecuta mover(direccion, puedeAvanzar).
     */
    public void mover(Direccion direccion, Laberinto laberinto) {
        Posicion candidata = calcularProximaPosicion(direccion);
        boolean puedeAvanzar = laberinto != null && laberinto.esTransitable(candidata);
        mover(direccion, puedeAvanzar);
    }

    /**
     * Metodo sobrecargado por compatibilidad con firmas previas.
     */
    public Posicion calcularProximaPosicion(Direccion direccion, Laberinto laberinto) {
        Posicion candidata = calcularProximaPosicion(direccion);
        if (laberinto != null && laberinto.esTransitable(candidata)) {
            return candidata;
        }
        return new Posicion(this.posicion.fila, this.posicion.columna);
    }

    public abstract void volverAlInicio();

    public Posicion getPosicion() {
        return new Posicion(posicion.fila, posicion.columna);
    }

    public Direccion getDireccion() {
        return direccion;
    }

    protected Posicion getPosicionInicial() {
        return new Posicion(posicionInicial.fila, posicionInicial.columna);
    }
}
