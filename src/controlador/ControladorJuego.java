package controlador;

import dto.FantasmaDTO;
import dto.PartidaDTO;
import dto.PosicionDTO;
import dto.RankingDTO;
import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import java.util.ArrayList;
import java.util.List;
import modelo.EntradaRanking;
import modelo.Partida;
import modelo.Posicion;
import modelo.Ranking;

public class ControladorJuego {

    private static ControladorJuego instancia;
    private static final double SEGUNDOS_POR_TICK = 1.0 / 24.0;
    private Partida partida;
    private final Ranking ranking;
    private long ticks;
    private double tiempoSimulado;

    private ControladorJuego()
    {
        this.partida = new Partida();
        this.ranking = new Ranking();
    }

    public static ControladorJuego getInstance()
    {
        if (instancia == null) {
            instancia = new ControladorJuego();
        }
        return instancia;
    }

    public void moverPacMan(Direccion direccion)
    {
        if (this.partida.getEstado() == EstadoPartida.EN_CURSO) {
            this.partida.elegirDireccionPacman(direccion);
        }
    }

    public void avanzarTurno(Direccion direccion)
    {
        if (this.partida.getEstado() != EstadoPartida.EN_CURSO) {
            return;
        }
        moverPacMan(direccion);
        avanzarTurno();
    }

    public void avanzarTurno()
    {
        actualizar(SEGUNDOS_POR_TICK);
    }

    public void actualizar(double tiempoTranscurrido)
    {
        if (this.partida.getEstado() != EstadoPartida.EN_CURSO) {
            return;
        }
        this.partida.actualizar(tiempoTranscurrido);
        this.ticks++;
        this.tiempoSimulado += tiempoTranscurrido;

        EstadoPartida estadoActual = this.partida.getEstado();
        if (
            (estadoActual == EstadoPartida.DERROTA || estadoActual == EstadoPartida.VICTORIA)
            && !this.partida.isPuntajeRegistrado()
        ) {
            this.ranking.agregar(this.partida.getNombreJugador(), this.partida.getPuntaje());
            this.partida.marcarPuntajeRegistrado();
        }
    }

    public long getTicks()
    {
        return this.ticks;
    }

    public double getTiempoSimulado()
    {
        return this.tiempoSimulado;
    }

    public PosicionDTO getPosicionPacMan()
    {
        Posicion posicion = this.partida.getPosicionPacMan();
        return new PosicionDTO(posicion.fila, posicion.columna);
    }

    public Direccion getDireccionPacMan()
    {
        return this.partida.getDireccionPacMan();
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

    public void alternarPausa()
    {
        if (this.partida.getEstado() == EstadoPartida.PAUSADA) {
            reanudar();
        } else {
            pausar();
        }
    }

    public void iniciarPartida(String nombreJugador)
    {
        this.partida = new Partida(nombreJugador);
        this.ticks = 0;
        this.tiempoSimulado = 0;
    }

    public boolean nuevaPartida()
    {
        EstadoPartida estadoActual = this.partida.getEstado();

        if (
            estadoActual == EstadoPartida.VICTORIA
            || estadoActual == EstadoPartida.DERROTA
        ) {
            this.partida = new Partida(this.partida.getNombreJugador());
            this.ticks = 0;
            this.tiempoSimulado = 0;
            return true;
        }
        return false;
    }
    
    public PosicionDTO[] getPosicionesFantasmas()
    {
        Posicion[] posiciones = this.partida.getPosicionesFantasmas();
        PosicionDTO[] posicionesDTO = new PosicionDTO[posiciones.length];
        for (int i = 0; i < posiciones.length; i++) {
            posicionesDTO[i] = new PosicionDTO(posiciones[i].fila, posiciones[i].columna);
        }
        return posicionesDTO;
    }

    public EstadoFantasma[] getEstadosFantasmas()
    {
        return this.partida.getEstadosFantasmas();
    }

    public List<FantasmaDTO> getFantasmasDTO()
    {
        Posicion[] posiciones = this.partida.getPosicionesFantasmas();
        EstadoFantasma[] estados = this.partida.getEstadosFantasmas();
        List<FantasmaDTO> lista = new ArrayList<>(posiciones.length);

        for (int i = 0; i < posiciones.length; i++) {
            lista.add(new FantasmaDTO(
                posiciones[i].fila,
                posiciones[i].columna,
                estados[i].name()
            ));
        }
        return lista;
    }

    public PartidaDTO getPartidaDTO()
    {
        Posicion posPac = this.partida.getPosicionPacMan();
        Direccion dirPac = this.partida.getDireccionPacMan();

        return new PartidaDTO(
            this.partida.getNombreJugador(),
            this.partida.getPuntaje(),
            this.partida.getVidas(),
            this.partida.getEstado().name(),
            posPac.fila,
            posPac.columna,
            dirPac != null ? dirPac.name() : "DERECHA",
            getFantasmasDTO()
        );
    }

    public List<RankingDTO> getRanking()
    {
        List<EntradaRanking> entradas = this.ranking.getEntradas();
        List<RankingDTO> resultado = new ArrayList<>(entradas.size());
        for (EntradaRanking entrada : entradas) {
            resultado.add(new RankingDTO(entrada.getNombre(), entrada.getPuntaje()));
        }
        return resultado;
    }

    public String getEstadoTexto()
    {
        return this.partida.getEstado().name();
    }

}
