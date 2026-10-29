package modelo;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.Posicion;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        this.estado = EstadoFantasma.PERSIGUIENDO;
        this.tiempoSustoRestante = 0;
    }

    public Direccion elegirDireccion(
        Laberinto laberinto,
        Posicion posicionPacman
    )
    {
        if(this.estado == EstadoFantasma.ASUSTADO){
            // Fantasma Asustado
            List<Direccion> direcciones = laberinto.direccionesDisponibles(posicion);
            if(direcciones.isEmpty()){
                return null;
            }
            Random random = new Random();
            int proxima = random.nextInt(direcciones.size());
            return direcciones.get(proxima);
            // Fantasma Asustado
        }
        
        else{
            // Fantasma NO Asustado
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
            
            // Fantasma NO Asustado
        }
        return null;
        // Actualmente, Fantasma se mueve de manera totalmente aleatoria. 
        // Podríamos buscar la forma de que se mueva de manera coherente, buscando el camino más lejano a Pacman.
    }
    
    
    
    public void asustar(double duracion)
    {
        if(this.estado==EstadoFantasma.REGRESANDO){
            return;
        }
        this.estado = EstadoFantasma.ASUSTADO;
        tiempoSustoRestante = duracion;
    }

    public void serComido()
    {
        if(this.estado != EstadoFantasma.ASUSTADO){
            return;
        }
        this.estado = EstadoFantasma.REGRESANDO;
        this.tiempoSustoRestante = 0;
    }

    public void actualizarEstado(double tiempoTranscurrido)
    {
        if(this.estado == EstadoFantasma.ASUSTADO){
            this.tiempoSustoRestante -= tiempoTranscurrido;
            
            // Tolerancia para el redondeo al descontar fracciones como 1/24.
            if(tiempoSustoRestante <= 1e-9){
                tiempoSustoRestante = 0;
                this.estado = EstadoFantasma.PERSIGUIENDO;
            }
        }
        else if(this.estado == EstadoFantasma.REGRESANDO){
            if(this.posicion.fila == this.posicionInicial.fila &&
               this.posicion.columna == this.posicionInicial.columna){
                this.tiempoSustoRestante = 0;
                this.estado = EstadoFantasma.PERSIGUIENDO;
            }
        }
    }

    public EstadoFantasma getEstado()
    { 
        return this.estado; 
    }
}
