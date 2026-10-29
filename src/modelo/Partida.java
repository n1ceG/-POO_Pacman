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
        for(int i = 0; i < 4; i++){
            fantasmas[i] = new Fantasma(centro);
        }
    }

    public void actualizar(double tiempoTranscurrido)
    {
        if (this.estado != EstadoPartida.EN_CURSO) {
            return;
        }

        if (comprobarVictoria()) {
            this.estado = EstadoPartida.VICTORIA;
            return;
        }

        Direccion direccionPacMan = this.pacMan.getDireccion();

        Posicion proximaPosicionPacMan =
            this.pacMan.calcularProximaPosicion(
                direccionPacMan,
                this.laberinto
            );

        Celda celdaDestino = this.laberinto.obtenerCelda(
            proximaPosicionPacMan
        );

        Punto puntoConsumido = null;

        if (celdaDestino != null) {
            puntoConsumido = celdaDestino.retirarPunto();
            consumir(puntoConsumido);
        }

        boolean sustoRenovado = puntoConsumido instanceof SuperPunto;

        if (comprobarVictoria()) {
            this.pacMan.mover(direccionPacMan, this.laberinto);
            this.estado = EstadoPartida.VICTORIA;
            return;
        }

        Direccion[] direccionesFantasmas =
            new Direccion[this.fantasmas.length];

        List<Posicion> proximasPosicionesFantasmas =
            new ArrayList<>(this.fantasmas.length);

        for (int i = 0; i < this.fantasmas.length; i++) {
            direccionesFantasmas[i] =
                this.fantasmas[i].elegirDireccion(
                    this.laberinto,
                    proximaPosicionPacMan
                );

            proximasPosicionesFantasmas.add(
                this.fantasmas[i].calcularProximaPosicion(
                    direccionesFantasmas[i],
                    this.laberinto
                )
            );
        }

        // Detectar antes de mover conserva las posiciones anteriores
        // necesarias para reconocer intercambios de celdas.
        List<Fantasma> fantasmasTocados = detectarColision(
            proximaPosicionPacMan,
            proximasPosicionesFantasmas
        );

        this.pacMan.mover(direccionPacMan, this.laberinto);

        int vidasAntes = this.vidas;

        for (int i = 0; i < this.fantasmas.length; i++) {
            Fantasma fantasma = this.fantasmas[i];

            fantasma.mover(
                direccionesFantasmas[i],
                this.laberinto
            );

            if (fantasmasTocados.contains(fantasma)) {
                resolverColision(fantasma);

                if (this.vidas < vidasAntes) {
                    return;
                }
            }
        }

        // Un superpunto recién consumido conserva sus 10 segundos.
        double tiempoADescontar = sustoRenovado ? 0 : tiempoTranscurrido;

        for (Fantasma fantasma : this.fantasmas) {
            fantasma.actualizarEstado(tiempoADescontar);
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
        List<Fantasma> fantasmasTocados = new ArrayList<>(this.fantasmas.length);
        Posicion posicionPacman = this.pacMan.getPosicion();

        for (int i = 0; i < this.fantasmas.length; i++) {
            Fantasma fantasma = this.fantasmas[i];
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
        if (this.estado != EstadoPartida.EN_CURSO || this.vidas <= 0) {
            return;
        }
    
        this.vidas--;
    
        this.pacMan.volverAlInicio();
    
        for (Fantasma fantasma : this.fantasmas) {
            fantasma.volverAlInicio();
        }
    
        if (this.vidas == 0) {
            this.estado = EstadoPartida.DERROTA;
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

}