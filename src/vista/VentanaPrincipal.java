package vista;

import controlador.ControladorJuego;
import controlador.dto.PartidaDTO;
import extra.Direccion;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class VentanaPrincipal extends JFrame {

    private static final int INTERVALO_MS = 200;

    private final ControladorJuego controlador;
    private PanelLaberinto tablero;
    private Timer temporizador;
    private JLabel etiquetaPuntaje;
    private JLabel etiquetaVidas;
    private JLabel etiquetaEstado;
    private JButton botonPausa;
    private JButton botonReiniciar;
    private boolean esperandoInicio = true;

    public VentanaPrincipal() {
        controlador = new ControladorJuego();

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
        setLayout(new BorderLayout());
    }

    private void crearPaneles() {
        JPanel informacion = new JPanel();
        tablero = new PanelLaberinto(controlador);
        JPanel controles = new JPanel();

        informacion.setPreferredSize(new Dimension(0, 60));
        controles.setPreferredSize(new Dimension(0, 60));

        etiquetaPuntaje = new JLabel();
        etiquetaVidas = new JLabel();
        etiquetaEstado = new JLabel();

        informacion.add(etiquetaPuntaje);
        informacion.add(new JLabel("   |   "));
        informacion.add(etiquetaVidas);
        informacion.add(new JLabel("   |   "));
        informacion.add(etiquetaEstado);

        botonPausa = new JButton("Pausar");
        botonPausa.addActionListener(e -> {
            controlador.alternarPausa();
            actualizarInformacion();
            tablero.repaint();
        });
        // Las flechas siguen disponibles para controlar el tablero.
        botonPausa.setFocusable(false);

        botonReiniciar = new JButton("Nueva Partida");
        botonReiniciar.addActionListener(e -> {
            if (controlador.nuevaPartida()) {
                esperandoInicio = true;
                actualizarInformacion();
                tablero.repaint();
            }
        });
        botonReiniciar.setFocusable(false);

        controles.add(botonPausa);
        controles.add(botonReiniciar);

        add(informacion, BorderLayout.NORTH);
        add(tablero, BorderLayout.CENTER);
        add(controles, BorderLayout.SOUTH);

        actualizarInformacion();
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
            controlador.alternarPausa();
            actualizarInformacion();
            tablero.repaint();
        });

        asociarAccion("N", () -> {
            if (controlador.nuevaPartida()) {
                esperandoInicio = true;
                actualizarInformacion();
                tablero.repaint();
            }
        });
    }

    private void asociarTeclaDireccion(String tecla, Direccion direccion) {
        asociarAccion(tecla, () -> {
            esperandoInicio = false;
            controlador.moverPacMan(direccion);
        });
    }

    private void asociarAccion(String tecla, Runnable accion) {
        tablero.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(tecla), tecla);

        tablero.getActionMap().put(tecla, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accion.run();
            }
        });
    }

    private void iniciarTemporizador() {
        temporizador = new Timer(INTERVALO_MS, e -> {
            if (!esperandoInicio) {
                controlador.actualizar(INTERVALO_MS / 1000.0);
            }
            actualizarInformacion();
            tablero.repaint();
        });

        temporizador.start();
    }

    private void actualizarInformacion() {
        PartidaDTO partida = controlador.getPartidaDTO();
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
