package modelo;

import extra.Direccion;
import extra.Posicion;
import java.util.LinkedList;
import java.util.List;

public class Laberinto {

    private Celda[][] celdas;
    private int puntosRestantes = 0;

    public Laberinto(){
        final int cantCeldas = 20;
        celdas = new Celda[cantCeldas][cantCeldas];
        for(int i = 0; i<cantCeldas;i++){
            for(int j = 0; j<cantCeldas;j++){
                if(
                    (i==0&&j==0) ||
                    (i==0&&j==cantCeldas-1)||
                    (i==cantCeldas-1&&j==0)||
                    (i==cantCeldas-1&&j==cantCeldas-1)
                ){
                    celdas[i][j] = new Celda(
                        false, 
                        true, 
                        i, 
                        j
                    );
                }else{
                    celdas[i][j] = new Celda(
                        false, 
                        false, 
                        i, 
                        j
                    );
                }
            }
        }
    }

    public boolean esTransitable(Posicion posicion)
    {   
        return !obtenerCelda(posicion).esPared();
    }

    public List<Direccion> direccionesDisponibles(Posicion posicion)
    { 
        List<Direccion> lista = new LinkedList<>();
        int x = posicion.getPosicion()[0];
        int y = posicion.getPosicion()[1];
        if(!celdas[x][y-1].esPared()){lista.add(Direccion.ARRIBA);}
        if(!celdas[x][y+1].esPared()){lista.add(Direccion.ABAJO);}
        if(!celdas[x-1][y].esPared()){lista.add(Direccion.IZQUIERDA);}
        if(!celdas[x+1][y].esPared()){lista.add(Direccion.DERECHA);}
        return lista;
    }
    
    public List<Posicion> caminoMasCorto(
        Posicion origen,
        Posicion destino
    )
    {
        return null;
    }
    
    public boolean quedanPuntos()
    { return false; }
    
    public Posicion centro()
    { return null; }
    
    public Celda obtenerCelda(Posicion posicion)
    { 
        int x = posicion.getPosicion()[0];
        int y = posicion.getPosicion()[1];
        return this.celdas[x][y];
    }
    
    private void imprimirLaberinto(){
        for(Celda[] celdas:this.celdas){
            for(Celda c :celdas){
                if(c.tipoPunto().getClass().equals(PuntoComun.class)){
                    System.out.print("1 ");
                }
                else if(c.tipoPunto().getClass().equals(SuperPunto.class)){
                    System.out.print("2 ");
                }
                else{
                    System.out.print("0 ");
                }
            }
            System.out.println();
        }
        // for(Celda[] celdas:this.celdas){
        //     for(Celda c :celdas){
        //         System.out.print("a");
        //     }
        //     System.out.println();
        // }
    }
    void main() {

        imprimirLaberinto();
    }
}
