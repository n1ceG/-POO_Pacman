package vista;

import controlador.ControladorJuego;
import dto.PartidaDTO;
import dto.RankingDTO;
import extra.Direccion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class VentanaPrincipal extends JFrame {

    private static final int INTERVALO_MS = 200;

    private PanelLaberinto tablero;
    private Timer temporizador;
    private JLabel etiquetaPuntaje;
    private JLabel etiquetaVidas;
    private JLabel etiquetaEstado;
    private JButton botonPausa;
    private JButton botonReiniciar;
    private CardLayout cardLayout;
    private JPanel contenedor;
    private DefaultTableModel modeloTablaRanking;
    private JLabel etiquetaJugador;
    private JTextField campoNombre;
    private boolean esperandoInicio = true;
    private boolean enJuego = false;

    public VentanaPrincipal() {
        configurarVentana();
        crearPaneles();
        configurarTeclado();

        pack();
        setLocationRelativeTo(null);

        iniciarTemporizador();
    }

    private void configurarVentana() {
        setTitle("Pac-Man");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void crearPaneles() {
        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);

        contenedor.add(crearPanelInicio(), "INICIO");
        contenedor.add(crearPanelJuego(), "JUEGO");
        contenedor.add(crearPanelRanking(), "RANKING");

        setContentPane(contenedor);
        cardLayout.show(contenedor, "INICIO");
    }

    private JPanel crearPanelInicio() {
        JPanel panelInicio = new JPanel(new BorderLayout(10, 10));

        JPanel centro = new JPanel(new FlowLayout());
        centro.add(new JLabel("Nombre del jugador:"));
        campoNombre = new JTextField(15);
        centro.add(campoNombre);

        JPanel sur = new JPanel(new FlowLayout());
        JButton botonIniciar = new JButton("Iniciar Partida");
        sur.add(botonIniciar);

        botonIniciar.addActionListener(e -> iniciarJuegoConNombre());
        campoNombre.addActionListener(e -> iniciarJuegoConNombre());

        panelInicio.add(centro, BorderLayout.CENTER);
        panelInicio.add(sur, BorderLayout.SOUTH);
        return panelInicio;
    }

    private void iniciarJuegoConNombre() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresá tu nombre.");
            campoNombre.requestFocus();
            return;
        }

        ControladorJuego.getInstance().iniciarPartida(nombre);
        esperandoInicio = true;
        enJuego = true;
        actualizarInformacion();
        tablero.repaint();
        cardLayout.show(contenedor, "JUEGO");
        tablero.requestFocusInWindow();
        temporizador.start();
    }

    private JPanel crearPanelJuego() {
        JPanel panelJuego = new JPanel(new BorderLayout());
        JPanel informacion = new JPanel();
        tablero = new PanelLaberinto();
        JPanel controles = new JPanel();

        informacion.setPreferredSize(new Dimension(0, 60));
        controles.setPreferredSize(new Dimension(0, 60));

        etiquetaJugador = new JLabel();
        etiquetaPuntaje = new JLabel();
        etiquetaVidas = new JLabel();
        etiquetaEstado = new JLabel();

        informacion.add(etiquetaJugador);
        informacion.add(new JLabel("   |   "));
        informacion.add(etiquetaPuntaje);
        informacion.add(new JLabel("   |   "));
        informacion.add(etiquetaVidas);
        informacion.add(new JLabel("   |   "));
        informacion.add(etiquetaEstado);

        botonPausa = new JButton("Pausar");
        botonPausa.addActionListener(e -> {
            ControladorJuego.getInstance().alternarPausa();
            actualizarInformacion();
            tablero.repaint();
        });
        // Las flechas siguen disponibles para controlar el tablero.
        botonPausa.setFocusable(false);

        botonReiniciar = new JButton("Nueva Partida");
        botonReiniciar.addActionListener(e -> {
            if (ControladorJuego.getInstance().nuevaPartida()) {
                esperandoInicio = true;
                actualizarInformacion();
                tablero.repaint();
            }
        });
        botonReiniciar.setFocusable(false);

        controles.add(botonPausa);
        controles.add(botonReiniciar);

        panelJuego.add(informacion, BorderLayout.NORTH);
        panelJuego.add(tablero, BorderLayout.CENTER);
        panelJuego.add(controles, BorderLayout.SOUTH);

        actualizarInformacion();
        return panelJuego;
    }

    private JPanel crearPanelRanking() {
        JPanel panelRanking = new JPanel(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Tabla de Rankings", SwingConstants.CENTER);
        panelRanking.add(titulo, BorderLayout.NORTH);

        String[] columnas = {"Puesto", "Jugador", "Puntaje"};
        modeloTablaRanking = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(modeloTablaRanking);
        panelRanking.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout());
        JButton botonJugarDeNuevo = new JButton("Jugar de nuevo");
        JButton botonCambiarJugador = new JButton("Cambiar jugador");

        botonJugarDeNuevo.addActionListener(e -> {
            ControladorJuego.getInstance().nuevaPartida();
            esperandoInicio = true;
            enJuego = true;
            actualizarInformacion();
            tablero.repaint();
            cardLayout.show(contenedor, "JUEGO");
            tablero.requestFocusInWindow();
            temporizador.start();
        });

        botonCambiarJugador.addActionListener(e -> {
            enJuego = false;
            campoNombre.setText("");
            cardLayout.show(contenedor, "INICIO");
            campoNombre.requestFocusInWindow();
        });

        botones.add(botonJugarDeNuevo);
        botones.add(botonCambiarJugador);
        panelRanking.add(botones, BorderLayout.SOUTH);
        return panelRanking;
    }

    private void cargarTablaRanking() {
        modeloTablaRanking.setRowCount(0);
        List<RankingDTO> ranking = ControladorJuego.getInstance().getRanking();
        for (int i = 0; i < ranking.size(); i++) {
            RankingDTO item = ranking.get(i);
            modeloTablaRanking.addRow(new Object[] {i + 1, item.getNombre(), item.getPuntaje()});
        }
    }

    private void configurarTeclado() {
        asociarTeclaDireccion("UP", Direccion.ARRIBA);
        asociarTeclaDireccion("DOWN", Direccion.ABAJO);
        asociarTeclaDireccion("LEFT", Direccion.IZQUIERDA);
        asociarTeclaDireccion("RIGHT", Direccion.DERECHA);

        asociarTeclaDireccion("W", Direccion.ARRIBA);
        asociarTeclaDireccion("S", Direccion.ABAJO);
        asociarTeclaDireccion("A", Direccion.IZQUIERDA);
        asociarTeclaDireccion("D", Direccion.DERECHA);

        asociarAccion("P", () -> {
            ControladorJuego.getInstance().alternarPausa();
            actualizarInformacion();
            tablero.repaint();
        });

        asociarAccion("N", () -> {
            if (ControladorJuego.getInstance().nuevaPartida()) {
                esperandoInicio = true;
                actualizarInformacion();
                tablero.repaint();
            }
        });
    }

    private void asociarTeclaDireccion(String tecla, Direccion direccion) {
        asociarAccion(tecla, () -> {
            esperandoInicio = false;
            ControladorJuego.getInstance().moverPacMan(direccion);
        });
    }

    private void asociarAccion(String tecla, Runnable accion) {
        tablero.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(tecla), tecla);

        tablero.getActionMap().put(tecla, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enJuego) {
                    return;
                }
                accion.run();
            }
        });
    }

    private void iniciarTemporizador() {
        temporizador = new Timer(INTERVALO_MS, e -> {
            if (!esperandoInicio) {
                ControladorJuego.getInstance().actualizar(INTERVALO_MS / 1000.0);
            }
            actualizarInformacion();
            tablero.repaint();

            if (ControladorJuego.getInstance().getPartidaDTO().esDerrota()) {
                temporizador.stop();
                enJuego = false;
                cargarTablaRanking();
                cardLayout.show(contenedor, "RANKING");
            }
        });
    }

    private void actualizarInformacion() {
        PartidaDTO partida = ControladorJuego.getInstance().getPartidaDTO();
        etiquetaJugador.setText("Jugador: " + partida.getNombreJugador());
        etiquetaPuntaje.setText("Puntaje: " + partida.getPuntaje());
        etiquetaVidas.setText("Vidas: " + partida.getVidas());

        String textoEstado;
        if (partida.estaPausada()) {
            textoEstado = "Pausada";
        } else if (partida.esVictoria()) {
            textoEstado = "¡VICTORIA!";
        } else if (partida.esDerrota()) {
            textoEstado = "DERROTA";
        } else {
            textoEstado = esperandoInicio ? "¡Listo! (Elija una dirección)" : "En curso";
        }
        etiquetaEstado.setText("Estado: " + textoEstado);

        if (partida.estaPausada()) {
            botonPausa.setText("Reanudar");
        } else {
            botonPausa.setText("Pausar");
        }

        boolean finalizada = partida.estaFinalizada();
        botonPausa.setEnabled(!finalizada);
        botonReiniciar.setEnabled(finalizada);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
