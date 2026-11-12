package modelo;

import dto.Posicion;

public class Celda {

    private final Posicion posicion;
    private final boolean pared;
    private final int valor;
    private boolean fueComido;

    public Celda(boolean esPared, boolean esSuperPunto, int fila, int columna) {
        this(esPared, esSuperPunto ? 50 : 10, fila, columna);
    }

    public Celda(boolean esPared, int valor, int fila, int columna) {
        if (valor != 0 && valor != 10 && valor != 50) {
            throw new IllegalArgumentException("El valor debe ser 0, 10 o 50");
        }
        this.posicion = new Posicion(fila, columna);
        this.pared = esPared;
        this.valor = esPared ? 0 : valor;
        this.fueComido = false;
    }

    public int retirarPunto() {
        int retirado = getValor();
        if (retirado > 0) {
            this.fueComido = true; 
        }
        return retirado;
    }

    public Posicion getPosicion() {
        return new Posicion(posicion.fila, posicion.columna);
    }

    public int getValor() {
        return fueComido ? 0 : valor;
    }

    public boolean esPared() {
        return pared;
    }

    public boolean tienePunto() {
        return getValor() > 0;
    }
}
