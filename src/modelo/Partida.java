package modelo;

import extra.Direccion;
import extra.EstadoFantasma;
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
    private static final double DURACION_SUSTO = 10;

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

        if (comprobarVictoria()) {
            this.estado = EstadoPartida.VICTORIA;
        }
        for(Fantasma f:fantasmas){
            f.mover(f.elegirDireccion(laberinto, pacMan.getPosicion()), laberinto);
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
        this.puntaje += punto.valor();

        if (punto instanceof SuperPunto) {
            for (Fantasma fantasma : fantasmas) {
                fantasma.asustar(DURACION_SUSTO);
            }
        }
    }
    

    public void resolverColision(Fantasma fantasma)
    {
        if (fantasma.getEstado() == EstadoFantasma.ASUSTADO) {
            this.puntaje += 200;
            fantasma.serComido();
        }
    }

    public boolean comprobarVictoria()
    { 
        return !laberinto.quedanPuntos();
    }

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
        return this.pacMan.getPosicion();
    }

    public int getVidas()
    {
        return this.vidas;
    }

    public int getPuntaje()
    {
        return this.puntaje;
    }

    public EstadoPartida getEstado()
    {
        return this.estado;
    }

    public Posicion[] getPosicionesFantasmas()
    {
        Posicion[] posicionesFantasmas = new Posicion[this.fantasmas.length];
        for(int i = 0; i<posicionesFantasmas.length;i++){
            posicionesFantasmas[i] = this.fantasmas[i].getPosicion();
        }
        return posicionesFantasmas;
    }
    
    public EstadoFantasma[] getEstadosFantasmas()
    {
        EstadoFantasma[] estadosFantasmas = new EstadoFantasma[this.fantasmas.length];
        for(int i = 0; i<estadosFantasmas.length;i++){
            estadosFantasmas[i] = this.fantasmas[i].getEstado();
        }
        return estadosFantasmas;
    }


    public void nuevaPartida()
    {}
}