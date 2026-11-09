package modelo;

public class SuperPunto extends Punto {

    @Override
    public int valor() {
        return 50;
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
