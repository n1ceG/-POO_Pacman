package modelo;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.Posicion;
import java.util.ArrayList;
import java.util.List;

public class Fantasma extends Personaje {

    private EstadoFantasma estado;
    private double tiempoSustoRestante;

    public Fantasma(Posicion posicionInicial)
    {
        super(posicionInicial);
        this.estado = EstadoFantasma.PERSIGUIENDO;
    }

    @Override
    public void mover(Direccion direccion, Laberinto laberinto)
    {
        this.posicion = calcularProximaPosicion(direccion, laberinto);
    }

    @Override
    public void volverAlInicio()
    {
        this.posicion = getPosicionInicial();
    }

    public Direccion elegirDireccion(
        Laberinto laberinto,
        Posicion posicionPacman
    )
    {
        List<Posicion> caminoMasCorto = new ArrayList<>();
        
        if(this.estado==EstadoFantasma.PERSIGUIENDO){
            caminoMasCorto = laberinto.caminoMasCorto(posicion, posicionPacman);
        }
        if(this.estado==EstadoFantasma.REGRESANDO){
            caminoMasCorto = laberinto.caminoMasCorto(posicion, posicionInicial);
        }
        
        if(caminoMasCorto.isEmpty()){
            return null; 
        }
        if(caminoMasCorto.size()==1){ 
            return null; 
        }
        int siguienteX = caminoMasCorto.get(1).fila;
        int siguienteY = caminoMasCorto.get(1).columna;
        
        int actualX = this.posicion.fila;
        int actualY = this.posicion.columna;
        
        int diferenciaFila = siguienteX-actualX;
        int diferenciaColumna = siguienteY-actualY;
        
        if (diferenciaFila == -1) {
            return Direccion.ARRIBA;
        }
        if (diferenciaFila == 1) {
            return Direccion.ABAJO;
        }
        if (diferenciaColumna == -1) {
            return Direccion.IZQUIERDA;
        }
        if (diferenciaColumna == 1) {
            return Direccion.DERECHA;
        }
        
        return null;
        // Falta implementar el caso de un fantasma asustado
    }
    
    
    
    public void asustar(double duracion)
    {
        if(this.estado==EstadoFantasma.REGRESANDO){return;}
        this.estado = EstadoFantasma.ASUSTADO;
        tiempoSustoRestante = duracion;
    }

    public void serComido()
    {}

    public void actualizarEstado(double tiempoTranscurrido)
    {}

    public EstadoFantasma getEstado()
    { 
        return this.estado; 
    }
}
