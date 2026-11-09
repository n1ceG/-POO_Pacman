package modelo;

public class PuntoComun extends Punto {

    @Override
    public int valor() {
        return 10;
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
