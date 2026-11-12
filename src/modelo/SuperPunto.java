package modelo;

public class SuperPunto extends Punto {

    private final int valor = 50;

    @Override
    public int valor() {
        return this.valor;
    }

    @Override
    public boolean asustaFantasmas() {
        return true;
    }

    @Override
    public char getSimbolo() {
        return 'o';
    }
}
