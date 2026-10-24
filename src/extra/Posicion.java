package extra;

public class Posicion {

    public int fila;
    public int columna;

    public Posicion(int x, int y){
        this.fila = x;
        this.columna = y;
    }
    public int[] getPosicion(){ 
        return new int[]{fila, columna};
    }
    
    public void setPosicion(int x, int y){
        this.fila = x;
        this.columna = y;
    }
}
