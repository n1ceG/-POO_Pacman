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
    private Fantasma[] fantasmas;

    private static final Posicion INICIO_PACMAN = new Posicion(13, 7);
    private static final int VIDAS_INICIALES = 3;

    public Partida()
    {
        this(Laberinto.crearDefault(), INICIO_PACMAN);
    }

    public Partida(Laberinto laberinto, Posicion inicioPacMan)
    {
        this.vidas = VIDAS_INICIALES;
        this.puntaje = 0;
        this.estado = EstadoPartida.EN_CURSO;
        this.laberinto = laberinto;
        this.pacMan = new PacMan(inicioPacMan);
        this.fantasmas = new Fantasma[4];
        
        Posicion centro = laberinto.centro();
        for(int i = 0; i<4; i++){
            fantasmas[i] = new Fantasma(centro);
        }
    }


    public void actualizar(double tiempoTranscurrido)
    {
        if (estado != EstadoPartida.EN_CURSO) {
            return;
        }
        pacMan.mover(pacMan.getDireccion(), laberinto);
        Celda celda = laberinto.obtenerCelda(pacMan.getPosicion());
        if (celda != null) {
            consumir(celda.retirarPunto());
        }
        
    }
    
    public List<Fantasma> detectarColision(
        Posicion proximoPacman, 
        List<Posicion> proximosFantasmas
    )
    {
        return null;
    }

    public void consumir(Punto punto)
    {
        if (punto == null) {
            return;
        }

        puntaje += punto.valor();

    }

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

    public int getVidas()
    {
        return vidas;
    }

    public int getPuntaje()
    {
        return puntaje;
    }

    public EstadoPartida getEstado()
    {
        return estado;
    }
}
