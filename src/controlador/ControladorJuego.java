package controlador;

import extra.Direccion;
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
        partida.elegirDireccionPacman(direccion);
    }

    public void actualizar(double tiempoTranscurrido)
    {
        partida.actualizar(tiempoTranscurrido);
    }

    public Posicion getPosicionPacMan()
    {
        return partida.getPosicionPacMan();
    }

    public int getVidas()
    {
        return partida.getVidas();
    }

    public int getPuntaje()
    {
        return partida.getPuntaje();
    }

    public EstadoPartida getEstado()
    {
        return partida.getEstado();
    }

    public void pausar()
    {}

    public void reanudar()
    {}

    public void nuevaPartida()
    {}

}
