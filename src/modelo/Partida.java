package modelo;

import extra.Direccion;
import extra.EstadoPartida;
import extra.Posicion;
import java.util.List;

public class Partida {

    private int vidas;
    private int puntaje;
    private EstadoPartida estado;

    public void actualizar(double tiempoTranscurrido)
    {}
    
    public List<Fantasma> detectarColision(
        Posicion proximoPacman, 
        List<Posicion> proximosFantasmas
    )
    {
        return null;
    }

    public void consumir(Punto punto)
    {}

    public void resolverColision(Fantasma fantasma)
    {}

    public boolean comprobarVictoria()
    { return false; }

    public void perderVida()
    {}

    public void pausar()
    {}
    
    public void reanudar()
    {}

    public void elegirDireccionPacman(Direccion direccion)
    {}
}
