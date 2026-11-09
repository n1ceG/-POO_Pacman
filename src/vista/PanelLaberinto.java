package vista;

import controlador.ControladorJuego;
import controlador.dto.FantasmaDTO;
import controlador.dto.PartidaDTO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.List;
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

        PartidaDTO partida = controlador.getPartidaDTO();
        dibujarFantasmas(g, partida.getFantasmas());
        dibujarPacMan(g, partida);
        dibujarCartelEstado(g, partida);
    }

    private void dibujarPacMan(Graphics g, PartidaDTO partida) {
        int x = partida.getPacManColumna() * TAMANIO_CELDA;
        int y = partida.getPacManFila() * TAMANIO_CELDA;

        int anguloInicio = switch (partida.getPacManDireccion()) {
            case "ARRIBA" -> 120;
            case "IZQUIERDA" -> 210;
            case "ABAJO" -> 300;
            default -> 30; // DERECHA
        };

        g.setColor(Color.YELLOW);
        g.fillArc(
            x + 2,
            y + 2,
            TAMANIO_CELDA - 4,
            TAMANIO_CELDA - 4,
            anguloInicio,
            300
        );
    }

    private void dibujarFantasmas(Graphics g, List<FantasmaDTO> fantasmas) {
        Color[] colores = {
            Color.RED,
            Color.PINK,
            Color.CYAN,
            Color.ORANGE
        };

        for (int i = 0; i < fantasmas.size(); i++) {
            FantasmaDTO fantasma = fantasmas.get(i);
            int x = fantasma.getColumna() * TAMANIO_CELDA;
            int y = fantasma.getFila() * TAMANIO_CELDA;

            if (fantasma.estaRegresando()) {
                // Solo ojos cuando regresa a la base tras ser comido
                dibujarOjos(g, x, y, Color.BLUE);
                continue;
            }

            Color colorCuerpo = fantasma.estaAsustado()
                ? Color.BLUE
                : colores[i % colores.length];

            g.setColor(colorCuerpo);
            g.fillOval(
                x + 2,
                y + 2,
                TAMANIO_CELDA - 4,
                TAMANIO_CELDA - 4
            );

            if (fantasma.estaAsustado()) {
                dibujarOjos(g, x, y, Color.ORANGE);
            } else {
                dibujarOjos(g, x, y, Color.BLUE);
            }
        }
    }

    private void dibujarOjos(Graphics g, int x, int y, Color colorPupila) {
        // Esclerótica (blanco de los ojos)
        g.setColor(Color.WHITE);
        g.fillOval(x + 6, y + 6, 6, 8);
        g.fillOval(x + 15, y + 6, 6, 8);

        // Pupilas
        g.setColor(colorPupila);
        g.fillOval(x + 8, y + 9, 3, 4);
        g.fillOval(x + 17, y + 9, 3, 4);
    }

    private void dibujarCartelEstado(Graphics g, PartidaDTO partida) {
        if (partida.estaEnCurso()) {
            return;
        }

        int ancho = getWidth();
        int alto = getHeight();

        // Franja oscura semi-transparente centrada
        g.setColor(new Color(0, 0, 0, 195));
        g.fillRect(0, (alto / 2) - 45, ancho, 90);

        g.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fmTitulo = g.getFontMetrics();

        String titulo;
        Color colorTexto;
        String subtitulo;

        if (partida.estaPausada()) {
            titulo = "PAUSA";
            colorTexto = Color.YELLOW;
            subtitulo = "Presione Pausar (P) para reanudar";
        } else if (partida.esVictoria()) {
            titulo = "¡VICTORIA!";
            colorTexto = Color.GREEN;
            subtitulo = "Presione Nueva Partida (N) para jugar otra vez";
        } else if (partida.esDerrota()) {
            titulo = "GAME OVER";
            colorTexto = Color.RED;
            subtitulo = "Presione Nueva Partida (N) para jugar otra vez";
        } else {
            return;
        }

        g.setColor(colorTexto);
        int xTitulo = (ancho - fmTitulo.stringWidth(titulo)) / 2;
        int yTitulo = (alto / 2) - 10;
        g.drawString(titulo, xTitulo, yTitulo);

        g.setFont(new Font("Arial", Font.PLAIN, 13));
        FontMetrics fmSub = g.getFontMetrics();

        g.setColor(Color.WHITE);
        int xSub = (ancho - fmSub.stringWidth(subtitulo)) / 2;
        int ySub = (alto / 2) + 20;
        g.drawString(subtitulo, xSub, ySub);
    }
}
