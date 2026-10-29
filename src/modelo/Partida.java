package modelo;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.util.ArrayList;
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

        Posicion posicionAnteriorPacman = pacMan.getPosicion();

        pacMan.mover(pacMan.getDireccion(), laberinto);

        Celda celda = laberinto.obtenerCelda(pacMan.getPosicion());
        Punto punto = (celda != null) ? celda.retirarPunto() : null;
        consumir(punto);

        // El ultimo punto tiene prioridad sobre cualquier contacto de este paso.
        if (comprobarVictoria()) {
            this.estado = EstadoPartida.VICTORIA;
            return;
        }

        Direccion[] direcciones = new Direccion[fantasmas.length];
        List<Posicion> proximasPosiciones = new ArrayList<>(fantasmas.length);
        for (int i = 0; i < fantasmas.length; i++) {
            direcciones[i] = fantasmas[i].elegirDireccion(laberinto, pacMan.getPosicion());
            proximasPosiciones.add(
                fantasmas[i].calcularProximaPosicion(direcciones[i], laberinto)
            );
        }

        List<Fantasma> contactos = detectarColision(
            posicionAnteriorPacman,
            pacMan.getPosicion(),
            proximasPosiciones
        );

        // Contactos antes del vencimiento. Una perdida de vida corta el paso.
        for (int i = 0; i < fantasmas.length; i++) {
            Fantasma fantasma = fantasmas[i];
            fantasma.mover(direcciones[i], laberinto);

            if (contactos.contains(fantasma)) {
                int vidasAntes = this.vidas;
                resolverColision(fantasma);
                if (this.vidas != vidasAntes) {
                    return;
                }
            }
        }

        // Un susto recien activado empieza a descontarse en el paso siguiente.
        double tiempoEstado = (punto instanceof SuperPunto) ? 0 : tiempoTranscurrido;
        for (Fantasma fantasma : fantasmas) {
            fantasma.actualizarEstado(tiempoEstado);
        }
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
    
    public List<Fantasma> detectarColision(
        Posicion proximaPosicionPacman,
        List<Posicion> proximasPosicionesFantasmas
    )
    {
        return detectarColision(
            pacMan.getPosicion(),
            proximaPosicionPacman,
            proximasPosicionesFantasmas
        );
    }

    private List<Fantasma> detectarColision(
        Posicion posicionPacman,
        Posicion proximaPosicionPacman,
        List<Posicion> proximasPosicionesFantasmas
    )
    {
        List<Fantasma> fantasmasTocados = new ArrayList<>(fantasmas.length);

        for (int i = 0; i < fantasmas.length; i++) {
            Fantasma fantasma = fantasmas[i];
            Posicion actualFantasma = fantasma.getPosicion();
            Posicion proximaFantasma = proximasPosicionesFantasmas.get(i);

            boolean mismaLlegada =
                proximaPosicionPacman.fila == proximaFantasma.fila
                && proximaPosicionPacman.columna == proximaFantasma.columna;

            boolean intercambio =
                proximaPosicionPacman.fila == actualFantasma.fila
                && proximaPosicionPacman.columna == actualFantasma.columna
                && proximaFantasma.fila == posicionPacman.fila
                && proximaFantasma.columna == posicionPacman.columna;

            if (mismaLlegada || intercambio) {
                fantasmasTocados.add(fantasma);
            }
        }

        return fantasmasTocados;
    }

    public void resolverColision(Fantasma fantasma)
    {
        if (fantasma.getEstado() == EstadoFantasma.ASUSTADO) {
            this.puntaje += 200;
            fantasma.serComido();
        }
        else if (fantasma.getEstado() == EstadoFantasma.PERSIGUIENDO) {
            perderVida();
        }
    }

    public boolean comprobarVictoria()
    { 
        return !laberinto.quedanPuntos();
    }

    public void perderVida()
    {
        if (this.estado != EstadoPartida.EN_CURSO) {
            return;
        }

        this.vidas--;

        if (this.vidas == 0) {
            this.estado = EstadoPartida.DERROTA;
        }

        this.pacMan.volverAlInicio();
        for (Fantasma fantasma : fantasmas) {
            fantasma.volverAlInicio();
        }
    }

    public void pausar()
    {
        if (this.estado == EstadoPartida.EN_CURSO) {
            this.estado = EstadoPartida.PAUSADA;
        }
    }   

    public void reanudar()
    {
        if (this.estado == EstadoPartida.PAUSADA) {
            this.estado = EstadoPartida.EN_CURSO;
        }
    }

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