package controlador;

import extra.Direccion;
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

    public void pausar()
    {}

    public void reanudar()
    {}

    public void nuevaPartida()
    {}

}
