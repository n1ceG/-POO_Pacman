package controlador;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import modelo.Partida;

public class ControladorJuego {

    private Partida partida;

    public ControladorJuego()
    {
        this(new Partida());
    }

    public ControladorJuego(Partida partida)
    {
        this.partida = partida;
    }

    public void moverPacMan(Direccion direccion)
    {
        this.partida.elegirDireccionPacman(direccion);
    }

    public void actualizar(double tiempoTranscurrido)
    {
        this.partida.actualizar(tiempoTranscurrido);
    }

    public Posicion getPosicionPacMan()
    {
        return this.partida.getPosicionPacMan();
    }

    public int getVidas()
    {
        return this.partida.getVidas();
    }

    public char[][] getMapa()
    {
        return this.partida.getMapa();
    }

    public int getPuntaje()
    {
        return this.partida.getPuntaje();
    }

    public EstadoPartida getEstado()
    {
        return this.partida.getEstado();
    }

    public void pausar()
    {
        this.partida.pausar();
    }
    
    public void reanudar()
    {
        this.partida.reanudar();
    }

    public void nuevaPartida()
    {
        EstadoPartida estadoActual = this.partida.getEstado();

        if (
            estadoActual == EstadoPartida.VICTORIA
            || estadoActual == EstadoPartida.DERROTA
        ) {
            this.partida = new Partida();
        }
    }
    
    public Posicion[] getPosicionesFantasmas()
    {
        return this.partida.getPosicionesFantasmas();
    }

    public EstadoFantasma[] getEstadosFantasmas()
    {
        return this.partida.getEstadosFantasmas();
    }

}
