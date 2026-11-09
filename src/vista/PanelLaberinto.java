package vista;

import controlador.ControladorJuego;
import extra.EstadoFantasma;
import extra.Posicion;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class PanelLaberinto extends JPanel {

    private static final int TAMANIO_CELDA = 30;

    private final ControladorJuego controlador;

    public PanelLaberinto(ControladorJuego controlador) {
        this.controlador = controlador;

        setBackground(Color.BLACK);

        char[][] mapa = controlador.getMapa();

        int ancho = mapa[0].length * TAMANIO_CELDA;
        int alto = mapa.length * TAMANIO_CELDA;

        setPreferredSize(new Dimension(ancho, alto));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        char[][] mapa = controlador.getMapa();

        for (int fila = 0; fila < mapa.length; fila++) {
            for (int columna = 0; columna < mapa[fila].length; columna++) {

                int x = columna * TAMANIO_CELDA;
                int y = fila * TAMANIO_CELDA;

                switch (mapa[fila][columna]) {
                    case '#':
                        g.setColor(Color.BLUE);
                        g.fillRect(x, y, TAMANIO_CELDA, TAMANIO_CELDA);
                        break;

                    case '.':
                        g.setColor(Color.WHITE);
                        g.fillOval(x + 12, y + 12, 6, 6);
                        break;

                    case 'o':
                        g.setColor(Color.WHITE);
                        g.fillOval(x + 8, y + 8, 14, 14);
                        break;
                }
            }
        }
        dibujarFantasmas(g);
        dibujarPacMan(g);
    }

    private void dibujarPacMan(Graphics g){
        Posicion posicion = controlador.getPosicionPacMan();

        int x = posicion.columna * TAMANIO_CELDA;
        int y = posicion.fila * TAMANIO_CELDA;

        g.setColor(Color.YELLOW);
        g.fillArc(
            x + 2,
            y + 2,
            TAMANIO_CELDA - 4,
            TAMANIO_CELDA - 4,
            30,
            300
        );
    }

    private void dibujarFantasmas(Graphics g) {
        Posicion[] posiciones = controlador.getPosicionesFantasmas();
        EstadoFantasma[] estados = controlador.getEstadosFantasmas();

        Color[] colores = {
            Color.RED,
            Color.PINK,
            Color.CYAN,
            Color.ORANGE
        };

        for (int i = 0; i < posiciones.length; i++) {
            int x = posiciones[i].columna * TAMANIO_CELDA;
            int y = posiciones[i].fila * TAMANIO_CELDA;

            Color colorFantasma = switch (estados[i]) {
                case PERSIGUIENDO -> colores[i % colores.length];
                case ASUSTADO -> Color.BLUE;
                case REGRESANDO -> Color.GRAY;
            };

            g.setColor(colorFantasma);

            g.fillOval(
                x + 2,
                y + 2,
                TAMANIO_CELDA - 4,
                TAMANIO_CELDA - 4
            );
        }
    }


}
