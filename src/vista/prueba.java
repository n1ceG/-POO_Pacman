package vista;

import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoPartida;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class prueba extends JFrame{

    private static final int INTERVALO_MS = 200;

    private final ControladorJuego controlador;
    private PanelLaberinto tablero;
    private Timer temporizador;
    private JLabel etiquetaPuntaje;
    private JLabel etiquetaVidas;
    private JButton botonPausa;

    public prueba() {
        controlador = new ControladorJuego();

        configurarVentana();
        crearPaneles();
        configurarTeclado();

        pack();
        setLocationRelativeTo(null);

        iniciarTemporizador();
    }

    private void configurarVentana(){
        setTitle("Pacman");
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

        informacion.add(etiquetaPuntaje);
        informacion.add(etiquetaVidas);

        botonPausa = new JButton("Pausar");

        botonPausa.addActionListener(e -> {
            controlador.alternarPausa();
            actualizarInformacion();
        });

        // Las flechas siguen disponibles para controlar el tablero.
        botonPausa.setFocusable(false);

        controles.add(botonPausa);

        add(informacion, BorderLayout.NORTH);
        add(tablero, BorderLayout.CENTER);
        add(controles, BorderLayout.SOUTH);

        actualizarInformacion();
    }

    private void configurarTeclado() {
        asociarTecla("UP", Direccion.ARRIBA);
        asociarTecla("DOWN", Direccion.ABAJO);
        asociarTecla("LEFT", Direccion.IZQUIERDA);
        asociarTecla("RIGHT", Direccion.DERECHA);
    }

    private void asociarTecla(String tecla, Direccion direccion) {
        tablero.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(tecla), tecla);

        tablero.getActionMap().put(tecla, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.moverPacMan(direccion);
            }
        });
    }

    private void iniciarTemporizador() {
        temporizador = new Timer(INTERVALO_MS, e -> {
            controlador.actualizar(INTERVALO_MS / 1000.0);
            tablero.repaint();
        });

        temporizador.start();
    }

    private void actualizarInformacion() {
        etiquetaPuntaje.setText(
            "Puntaje: " + controlador.getPuntaje()
        );

        etiquetaVidas.setText(
            "Vidas: " + controlador.getVidas()
        );

        if (controlador.getEstado() == EstadoPartida.PAUSADA) {
            botonPausa.setText("Reanudar");
        } else {
            botonPausa.setText("Pausar");
        }

        boolean finalizada =
            controlador.getEstado() == EstadoPartida.VICTORIA
            || controlador.getEstado() == EstadoPartida.DERROTA;

        botonPausa.setEnabled(!finalizada);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            prueba ventana = new prueba();
            ventana.setVisible(true);
        });
    }
}
