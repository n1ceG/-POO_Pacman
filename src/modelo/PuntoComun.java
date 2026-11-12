package modelo;

public class PuntoComun extends Punto {

    private final int valor = 10;

    @Override
    public int valor() {
        return this.valor;
    }

    @Override
    public boolean asustaFantasmas() {
        return false;
    }

    @Override
    public char getSimbolo() {
        return '.';
    }
}
