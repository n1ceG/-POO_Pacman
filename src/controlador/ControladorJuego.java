package controlador;

import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import modelo.Partida;

public class ControladorJuego {

    private static final double SEGUNDOS_POR_TICK = 1.0 / 24.0;
    private Partida partida;
    private long ticks;
    private double tiempoSimulado;

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
    }

    public long getTicks()
    {
        return this.ticks;
    }

    public double getTiempoSimulado()
    {
        return this.tiempoSimulado;
    }

    public Posicion getPosicionPacMan()
    {
        return this.partida.getPosicionPacMan();
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

    public boolean nuevaPartida()
    {
        EstadoPartida estadoActual = this.partida.getEstado();

        if (
            estadoActual == EstadoPartida.VICTORIA
            || estadoActual == EstadoPartida.DERROTA
        ) {
            this.partida = new Partida();
            this.ticks = 0;
            this.tiempoSimulado = 0;
            return true;
        }
        return false;
    }
    
    public Posicion[] getPosicionesFantasmas()
    {
        return this.partida.getPosicionesFantasmas();
    }

    public EstadoFantasma[] getEstadosFantasmas()
    {
        return this.partida.getEstadosFantasmas();
    }

}
