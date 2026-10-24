package modelo;

import extra.Direccion;
import extra.EstadoPartida;
import extra.Posicion;
import java.util.List;

public class Partida {

    private int vidas;
    private int puntaje;
    private EstadoPartida estado;
    private Laberinto laberinto;
    private PacMan pacMan;

    private static final Posicion INICIO_PACMAN = new Posicion(13, 7);

    public Partida()
    {
        this(Laberinto.crearDefault(), INICIO_PACMAN);
    }

    public Partida(Laberinto laberinto, Posicion inicioPacMan)
    {
        this.laberinto = laberinto;
        this.pacMan = new PacMan(inicioPacMan);
        this.estado = EstadoPartida.EN_CURSO;
    }


    public void actualizar(double tiempoTranscurrido)
    {
        if (estado != EstadoPartida.EN_CURSO) {
            return;
        }
        pacMan.mover(pacMan.getDireccion(), laberinto);
    }
    
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
    {
        pacMan.elegirDireccion(direccion);
    }

    public Posicion getPosicionPacMan()
    {
        return pacMan.getPosicion();
    }
}
