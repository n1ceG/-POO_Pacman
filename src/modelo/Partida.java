package modelo;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
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
        int[] desplazamientos = {-2, -1, 1, 2};

        for (int i = 0; i < fantasmas.length; i++) {
            Posicion posicion = new Posicion(
                centro.fila,
                centro.columna + desplazamientos[i]
            );
            fantasmas[i] = new Fantasma(posicion);
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

        Posicion candidataPacMan = this.pacMan.calcularProximaPosicion(direccionPacMan);
        boolean puedeAvanzarPacMan = this.laberinto.esTransitable(candidataPacMan);
        Posicion proximaPosicionPacMan = puedeAvanzarPacMan
            ? candidataPacMan
            : this.pacMan.getPosicion();

        Celda celdaDestino = this.laberinto.obtenerCelda(
            proximaPosicionPacMan
        );

        int valorConsumido = 0;

        if (celdaDestino != null) {
            valorConsumido = celdaDestino.retirarPunto();
            consumir(valorConsumido);
        }

        boolean sustoRenovado = valorConsumido == 50;

        if (comprobarVictoria()) {
            if (puedeAvanzarPacMan) {
                this.pacMan.mover(direccionPacMan);
            }
            this.estado = EstadoPartida.VICTORIA;
            return;
        }

        Direccion[] direccionesFantasmas =
            new Direccion[this.fantasmas.length];

        boolean[] puedenAvanzarFantasmas =
            new boolean[this.fantasmas.length];

        List<Posicion> proximasPosicionesFantasmas =
            new ArrayList<>(this.fantasmas.length);

        for (int i = 0; i < this.fantasmas.length; i++) {
            direccionesFantasmas[i] =
                this.fantasmas[i].elegirDireccion(
                    this.laberinto,
                    proximaPosicionPacMan
                );

            Posicion candidataFantasma =
                this.fantasmas[i].calcularProximaPosicion(direccionesFantasmas[i]);
            boolean puedeAvanzar = this.laberinto.esTransitable(candidataFantasma);
            puedenAvanzarFantasmas[i] = puedeAvanzar;

            proximasPosicionesFantasmas.add(
                puedeAvanzar ? candidataFantasma : this.fantasmas[i].getPosicion()
            );
        }

        // Detectar antes de mover conserva las posiciones anteriores
        // necesarias para reconocer intercambios de celdas.
        List<Fantasma> fantasmasTocados = detectarColision(
            proximaPosicionPacMan,
            proximasPosicionesFantasmas
        );
        if (puedeAvanzarPacMan) {
            this.pacMan.mover(direccionPacMan);
        }

        for (int i = 0; i < this.fantasmas.length; i++) {
            if (puedenAvanzarFantasmas[i]) {
                this.fantasmas[i].mover(direccionesFantasmas[i]);
            }
        }

        // Cobrar todos los asustados antes de resolver la perdida de vida.
        boolean contactoPeligroso = false;
        for (Fantasma fantasma : fantasmasTocados) {
            if (fantasma.getEstado() == EstadoFantasma.ASUSTADO) {
                resolverColision(fantasma);
            } else if (fantasma.getEstado() == EstadoFantasma.PERSIGUIENDO) {
                contactoPeligroso = true;
            }
        }

        if (contactoPeligroso) {
            perderVida();
            return;
        }

        // Un superpunto recién consumido conserva sus 10 segundos.
        double tiempoADescontar = sustoRenovado ? 0 : tiempoTranscurrido;

        for (Fantasma fantasma : this.fantasmas) {
            fantasma.actualizarEstado(tiempoADescontar);
        }
    }
    
    public void consumir(int valor)
    {
        if (valor == 0) {
            return;
        }
        this.puntaje += valor;
        
        if (valor == 50) {
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

    public Direccion getDireccionPacMan()
    {
        return this.pacMan.getDireccion();
    }

    public char[][] getMapa()
    {
        return this.laberinto.getMapa();
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
