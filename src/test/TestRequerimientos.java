package test;

import consola.JuegoConsola;
import controlador.ControladorJuego;
import extra.Direccion;
import extra.EstadoFantasma;
import extra.EstadoPartida;
import extra.Posicion;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import modelo.Celda;
import modelo.Fantasma;
import modelo.Laberinto;
import modelo.PacMan;
import modelo.Partida;
import modelo.Personaje;
import modelo.PuntoComun;
import modelo.SuperPunto;

/**
 * Pruebas de los 10 puntos de "Requerimientos TPO PacMan 1.md".
 * Preparadas con asistencia de IA a pedido de Juan.
 *
 * Desde TPO-Pacman, con JDK 17 o superior:
 * javac --release 17 -encoding UTF-8 -d bin -sourcepath src src/test/TestRequerimientos.java
 * java -cp bin test.TestRequerimientos
 * Tambien se puede ejecutar el main desde el IDE.
 * No requiere JUnit, -ea, teclado ni esperas.
 * Cada caso crea su propio escenario. Un fallo no impide ejecutar los demas.
 * Salida del proceso: 0 si todo pasa; 1 si hay fallos.
 *
 * Se prueban el controlador, las reglas del modelo y la entrada de consola.
 * Los mapas pequenos aislan movimientos, tiempos y contactos. No representan
 * niveles jugables: algunos tienen un punto de reserva en otra habitacion
 * para evitar una victoria que interrumpa el comportamiento bajo prueba.
 *
 * La unica lectura por reflection obtiene los cuatro fantasmas de una Partida
 * para preparar contactos exactos. No escribe atributos privados: los mueve
 * por caminos transitables y cambia sus estados mediante sus metodos publicos.
 * La aleatoriedad se verifica por movimientos legales; los contactos con
 * asustados usan un extremo de pasillo con una unica salida.
 */
public class TestRequerimientos {
    private static final double TICK = 1.0 / 24.0;
    private static final String[] BASE = {
        "#############",
        "#           #",
        "#           #",
        "#           #",
        "#############",
        "#           #",
        "#           #",
        "#           #",
        "#############",
        "#           #",
        "#############"
    };
    private static int requisito;
    private static int casos;
    private static int comprobaciones;
    private static final int[] ejecutados = new int[11];
    private static final int[] fallidos = new int[11];

    public static void main(String[] args) {
        casos = 0;
        comprobaciones = 0;
        Arrays.fill(ejecutados, 0);
        Arrays.fill(fallidos, 0);
        System.out.println("PAC-MAN: pruebas de los 10 requerimientos\n");
        movimiento();
        paredes();
        puntaje();
        enemigos();
        superPunto();
        consumoFantasmas();
        muertePacMan();
        vidas();
        victoria();
        controlJuego();

        int errores = Arrays.stream(fallidos).sum();
        System.out.println("\nRESUMEN POR REQUERIMIENTO");
        for (int r = 1; r <= 10; r++) {
            System.out.printf("R%02d: %d/%d casos aprobados%s%n", r,
                ejecutados[r] - fallidos[r], ejecutados[r],
                fallidos[r] == 0 ? "" : "  REVISAR");
        }
        System.out.printf("%nTOTAL: %d/%d casos aprobados; %d comprobaciones ejecutadas.%n",
            casos - errores, casos, comprobaciones);
        if (errores > 0) {
            System.out.println("Hay fallos. Revisar los mensajes [FALLO] y sus valores.");
            System.exit(1);
        }
        System.out.println("Todos los casos aprobados.");
    }

    private static void movimiento() {
        grupo(1, "Movimiento");
        caso("Inicio hacia la derecha y un avance por tick", () -> {
            ControladorJuego c = new ControladorJuego();
            posicion("Inicio predeterminado", 13, 7, c.getPosicionPacMan());
            c.avanzarTurno();
            posicion("Primer avance", 13, 8, c.getPosicionPacMan());
            igual("Un tick", 1L, c.getTicks());
            cerca("Tiempo del turno", TICK, c.getTiempoSimulado());
        });
        for (Direccion d : Direccion.values()) {
            caso("Mover hacia " + d, () -> {
                Escenario e = escenario(2, 3);
                Posicion destino = vecino(new Posicion(2, 3), d);
                e.c.moverPacMan(d);
                posicion("Elegir direccion no mueve", 2, 3, e.c.getPosicionPacMan());
                igual("Elegir direccion no cuenta ticks", 0L, e.c.getTicks());
                e.c.avanzarTurno();
                posicion("Destino", destino, e.c.getPosicionPacMan());
            });
        }
        caso("Mantiene direccion y acepta giros entre celdas", () -> {
            Escenario e = escenario(2, 3);
            e.c.avanzarTurno(Direccion.DERECHA);
            e.c.avanzarTurno();
            posicion("Continua sin otra orden", 2, 5, e.c.getPosicionPacMan());
            e.c.avanzarTurno(Direccion.ARRIBA);
            posicion("Giro arriba", 1, 5, e.c.getPosicionPacMan());
            e.c.avanzarTurno(Direccion.IZQUIERDA);
            posicion("Giro izquierda", 1, 4, e.c.getPosicionPacMan());
        });
        caso("Un delta grande mueve solo una celda; direccion nula deja quieto", () -> {
            Escenario e = escenario(2, 3);
            e.c.actualizar(5);
            posicion("No salta cinco celdas", 2, 4, e.c.getPosicionPacMan());
            e.c.avanzarTurno(null);
            posicion("Sin direccion", 2, 4, e.c.getPosicionPacMan());
            cerca("Tiempo acumulado", 5 + TICK, e.c.getTiempoSimulado());
        });
        caso("Teclado W/A/S/D, Enter y mayusculas llegan al juego", () -> {
            String salida = consola("W\na\ns\nd\n\nq\n");
            contiene("W mueve arriba", salida, "Tick: 1", "F1:");
            contiene("A mueve izquierda", salida, "Tick: 2");
            contiene("S mueve abajo", salida, "Tick: 3");
            contiene("D mueve derecha", salida, "Tick: 4");
            contiene("Enter avanza", salida, "Tick: 5");
            // Se comprueban los tableros, ademas de que aumente el contador.
            String[] pantallas = salida.split("\033\\[2J\033\\[H");
            posicionEnPantalla(pantallas[2], 12, 7);
            posicionEnPantalla(pantallas[3], 12, 7); // A: hay pared en (12,6).
            posicionEnPantalla(pantallas[4], 13, 7);
            posicionEnPantalla(pantallas[5], 13, 8);
            posicionEnPantalla(pantallas[6], 13, 9);
            contiene("Salida por Q", salida, "Juego cerrado.");
        });
    }

    private static void paredes() {
        grupo(2, "Paredes y limites");
        for (Direccion d : Direccion.values()) {
            caso("No atraviesa pared hacia " + d, () -> {
                Escenario e = escenario(2, 3);
                PacMan p = new PacMan(new Posicion(2, 3));
                Posicion pared = vecino(p.getPosicion(), d);
                Celda[][] celdas = celdas(BASE);
                celdas[pared.fila][pared.columna] = new Celda(true, null, pared.fila, pared.columna);
                Laberinto mapa = new Laberinto(celdas);
                p.mover(d, mapa);
                posicion("PacMan permanece", 2, 3, p.getPosicion());
                Fantasma f = new Fantasma(new Posicion(2, 3));
                f.mover(d, mapa);
                posicion("Fantasma permanece", 2, 3, f.getPosicion());
                e.c.avanzarTurno(Direccion.ARRIBA);
                e.c.avanzarTurno();
                posicion("El controlador respeta el borde", 1, 3, e.c.getPosicionPacMan());
            });
        }
        caso("No sale del mapa aunque el borde no tenga pared", () -> {
            Laberinto mapa = new Laberinto(new String[] {"  ", "  "});
            for (Direccion d : Direccion.values()) {
                Posicion borde = switch (d) {
                    case ARRIBA, IZQUIERDA -> new Posicion(0, 0);
                    case ABAJO, DERECHA -> new Posicion(1, 1);
                };
                PacMan p = new PacMan(borde);
                p.mover(d, mapa);
                posicion("Limite " + d, borde, p.getPosicion());
            }
            igual("Celda fuera de rango", null, mapa.obtenerCelda(new Posicion(-1, 0)));
        });
        caso("Al bloquearse puede elegir una direccion libre", () -> {
            Escenario e = escenario(1, 3);
            e.c.avanzarTurno(Direccion.ARRIBA);
            posicion("Bloqueado", 1, 3, e.c.getPosicionPacMan());
            e.c.avanzarTurno(Direccion.DERECHA);
            posicion("Sale hacia la derecha", 1, 4, e.c.getPosicionPacMan());
        });
    }

    private static void puntaje() {
        grupo(3, "Puntos y superpuntos");
        caso("Mapa inicial: puntos en caminos y cuatro superpuntos en esquinas", () -> {
            ControladorJuego c = new ControladorJuego();
            char[][] mapa = c.getMapa();
            int supers = 0;
            int comunes = 0;
            for (int f = 0; f < mapa.length; f++) {
                for (int col = 0; col < mapa[f].length; col++) {
                    char celda = mapa[f][col];
                    comprobar(celda == '#' || celda == '.' || celda == 'o',
                        "Celda inicial con pared o punto en " + f + "," + col);
                    if (celda == '.') comunes++;
                    if (celda == 'o') supers++;
                }
            }
            igual("Cuatro superpuntos", 4, supers);
            comprobar(comunes > 0, "Hay puntos comunes");
            for (int f : new int[] {1, mapa.length - 2}) {
                for (int col : new int[] {1, mapa[0].length - 2}) {
                    igual("Superpunto en esquina " + f + "," + col, 'o', mapa[f][col]);
                }
            }
            igual("Valor comun", 10, new PuntoComun().valor());
            igual("Valor superpunto", 50, new SuperPunto().valor());
        });
        caso("Llegar consume un punto comun y suma 10 una sola vez", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 10);
            igual("Antes de llegar", 0, e.c.getPuntaje());
            e.c.avanzarTurno(Direccion.DERECHA);
            igual("Puntaje", 10, e.c.getPuntaje());
            igual("Retirado del mapa", ' ', e.c.getMapa()[2][4]);
            e.c.avanzarTurno(Direccion.IZQUIERDA);
            e.c.avanzarTurno(Direccion.DERECHA);
            e.c.avanzarTurno(null);
            igual("No vuelve a sumar", 10, e.c.getPuntaje());
        });
        caso("Llegar a superpunto suma 50 y no se repite", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 50);
            e.c.avanzarTurno();
            igual("Superpunto cobrado", 50, e.c.getPuntaje());
            e.c.avanzarTurno(null);
            igual("Superpunto no se cobra otra vez", 50, e.c.getPuntaje());
            igual("Superpunto retirado", ' ', e.c.getMapa()[2][4]);
        });
        caso("Puntos distintos acumulan y una celda vacia no suma", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 10, 2, 5, 50);
            e.c.avanzarTurno();
            e.c.avanzarTurno();
            e.c.avanzarTurno();
            igual("10 + 50 + vacia", 60, e.c.getPuntaje());
            e.p.consumir(null);
            igual("Consumo nulo no suma", 60, e.c.getPuntaje());
        });
        caso("Estar en la celda tambien permite consumir sin desplazarse", () -> {
            Escenario e = escenario(2, 3, true, 2, 3, 10);
            e.c.avanzarTurno(null);
            igual("Punto bajo PacMan", 10, e.c.getPuntaje());
            posicion("Sigue en la celda", 2, 3, e.c.getPosicionPacMan());
        });
        caso("Celda entrega y retira su punto; una pared no contiene puntos", () -> {
            PuntoComun punto = new PuntoComun();
            Celda celda = new Celda(false, punto, 1, 1);
            igual("Tipo inicial", punto, celda.tipoPunto());
            igual("Entrega el mismo punto", punto, celda.retirarPunto());
            comprobar(!celda.tienePunto(), "Queda vacia");
            igual("Segunda extraccion", null, celda.retirarPunto());
            comprobar(!new Celda(true, new SuperPunto(), 1, 1).tienePunto(),
                "Pared sin punto");
        });
        caso("El mapa y las posiciones consultadas no permiten modificar el juego", () -> {
            ControladorJuego c = new ControladorJuego();
            String antes = foto(c);
            c.getMapa()[1][1] = ' ';
            c.getPosicionPacMan().fila = 0;
            Posicion[] fantasmas = c.getPosicionesFantasmas();
            fantasmas[0].fila = 0;
            fantasmas[1] = new Posicion(0, 0);
            c.getEstadosFantasmas()[0] = EstadoFantasma.REGRESANDO;
            igual("Consultas son copias", antes, foto(c));
        });
    }

    private static void enemigos() {
        grupo(4, "Cuatro fantasmas autonomos");
        caso("Cuatro perseguidores separados alrededor del centro", () -> {
            ControladorJuego c = new ControladorJuego();
            Posicion[] posiciones = c.getPosicionesFantasmas();
            igual("Cantidad", 4, posiciones.length);
            int[] columnas = {5, 6, 8, 9};
            for (int i = 0; i < 4; i++) {
                posicion("Inicio fantasma " + i, 7, columnas[i], posiciones[i]);
                igual("Persigue " + i, EstadoFantasma.PERSIGUIENDO, c.getEstadosFantasmas()[i]);
                comprobar(c.getMapa()[7][columnas[i]] != '#', "Inicio transitable " + i);
            }
        });
        caso("Los cuatro avanzan solos una celda hacia PacMan", () -> {
            Escenario e = escenario(7, 1);
            Posicion[] antes = e.c.getPosicionesFantasmas();
            e.c.avanzarTurno(null);
            Posicion[] despues = e.c.getPosicionesFantasmas();
            for (int i = 0; i < 4; i++) {
                igual("Un paso del fantasma " + i, 1, distancia(antes[i], despues[i]));
                comprobar(distancia(despues[i], e.c.getPosicionPacMan())
                    < distancia(antes[i], e.c.getPosicionPacMan()), "Se acerca " + i);
            }
            igual("No alcanzo a PacMan", 3, e.c.getVidas());
        });
        caso("En un cruce elige el camino que acerca al objetivo", () -> {
            Laberinto mapa = new Laberinto(BASE);
            Fantasma f = new Fantasma(new Posicion(6, 6));
            for (Direccion d : Direccion.values()) {
                igual("Objetivo hacia " + d, d, f.elegirDireccion(mapa, vecino(f.getPosicion(), d)));
            }
        });
        caso("Rodea una pared aunque el primer paso se aleje del objetivo", () -> {
            Laberinto mapa = new Laberinto(new String[] {
                "#######", "#     #", "# ### #", "# #   #", "#     #", "#######"
            });
            Fantasma f = new Fantasma(new Posicion(3, 1));
            Posicion destino = new Posicion(3, 3);
            igual("Rodeo mas corto", Direccion.ABAJO, f.elegirDireccion(mapa, destino));
            List<Posicion> camino = mapa.caminoMasCorto(f.getPosicion(), destino);
            igual("Camino de cuatro pasos", 5, camino.size());
            for (int i = 1; i < camino.size(); i++) {
                igual("Celdas contiguas", 1, distancia(camino.get(i - 1), camino.get(i)));
                comprobar(mapa.esTransitable(camino.get(i)), "Paso sin pared");
            }
        });
        caso("Sin camino o ya en destino permanece quieto", () -> {
            Laberinto mapa = new Laberinto(BASE);
            Fantasma f = new Fantasma(new Posicion(5, 4));
            igual("Otra habitacion", null, f.elegirDireccion(mapa, new Posicion(2, 3)));
            igual("Misma celda", null, f.elegirDireccion(mapa, f.getPosicion()));
            f.mover(null, mapa);
            posicion("Quieto", 5, 4, f.getPosicion());
        });
    }

    private static void superPunto() {
        grupo(5, "Susto, aleatoriedad y duracion");
        caso("Superpunto asusta a los cuatro durante 10 segundos completos", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 50);
            e.c.actualizar(3); // Consumir no descuenta el delta del mismo tick.
            estados("Al consumir", e.c, EstadoFantasma.ASUSTADO);
            e.c.moverPacMan(null);
            e.c.actualizar(9.99);
            estados("Antes de los diez segundos", e.c, EstadoFantasma.ASUSTADO);
            e.c.actualizar(0.01);
            estados("Al vencer", e.c, EstadoFantasma.PERSIGUIENDO);
        });
        caso("El susto termina en el tick 240 posterior al consumo", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 50);
            e.c.avanzarTurno();
            e.c.moverPacMan(null);
            for (int i = 0; i < 239; i++) e.c.avanzarTurno();
            estados("239 ticks posteriores", e.c, EstadoFantasma.ASUSTADO);
            e.c.avanzarTurno();
            estados("240 ticks posteriores", e.c, EstadoFantasma.PERSIGUIENDO);
        });
        caso("Otro superpunto renueva a diez segundos", () -> {
            Escenario e = escenario(2, 2, true, 2, 3, 50, 2, 4, 50);
            e.c.avanzarTurno();
            e.c.moverPacMan(null);
            e.c.actualizar(7);
            e.c.avanzarTurno(Direccion.DERECHA);
            igual("Dos superpuntos", 100, e.c.getPuntaje());
            e.c.moverPacMan(null);
            e.c.actualizar(9.99);
            estados("No vence con el plazo anterior", e.c, EstadoFantasma.ASUSTADO);
            e.c.actualizar(0.01);
            estados("Vence con el nuevo plazo", e.c, EstadoFantasma.PERSIGUIENDO);
        });
        caso("El superpunto no vuelve a asustar a un fantasma REGRESANDO", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 50);
            ubicar(e.f[0], e.mapa, new Posicion(7, 4));
            e.f[0].asustar(10);
            e.f[0].serComido();
            e.c.avanzarTurno();
            igual("Regresando conserva estado", EstadoFantasma.REGRESANDO, e.f[0].getEstado());
            for (int i = 1; i < 4; i++) {
                igual("Otro fantasma asustado", EstadoFantasma.ASUSTADO, e.f[i].getEstado());
            }
        });
        caso("Asustado elige pasos legales sin exigir un resultado aleatorio concreto", () -> {
            Laberinto mapa = new Laberinto(BASE);
            Fantasma f = new Fantasma(new Posicion(6, 6));
            f.asustar(10);
            for (int i = 0; i < 100; i++) {
                Posicion antes = f.getPosicion();
                List<Direccion> disponibles = mapa.direccionesDisponibles(antes);
                Direccion d = f.elegirDireccion(mapa, new Posicion(1, 1));
                comprobar(disponibles.contains(d), "Eleccion entre las direcciones disponibles");
                f.mover(d, mapa);
                igual("Un paso", 1, distancia(antes, f.getPosicion()));
                comprobar(mapa.esTransitable(f.getPosicion()), "No atraviesa paredes");
            }
        });
        caso("Asustado sin salidas queda quieto", () -> {
            Laberinto mapa = new Laberinto(new String[] {"###", "# #", "###"});
            Fantasma f = new Fantasma(new Posicion(1, 1));
            f.asustar(10);
            Direccion d = f.elegirDireccion(mapa, new Posicion(1, 1));
            igual("Sin direccion posible", null, d);
            f.mover(d, mapa);
            posicion("No se desplaza", 1, 1, f.getPosicion());
        });
        caso("Superpunto se aplica antes del contacto del mismo tick", () -> {
            Escenario e = pasillo(true, 5, 2, 50);
            e.c.avanzarTurno(Direccion.IZQUIERDA);
            igual("50 + 200", 250, e.c.getPuntaje());
            igual("PacMan sobrevive", 3, e.c.getVidas());
            igual("Fantasma comido", EstadoFantasma.REGRESANDO, e.f[0].getEstado());
        });
    }

    private static void consumoFantasmas() {
        grupo(6, "Comer fantasmas y regreso al origen");
        caso("Comer un asustado suma 200 y no puede cobrarse dos veces", () -> {
            Escenario e = escenario(2, 3);
            e.f[0].asustar(10);
            e.p.resolverColision(e.f[0]);
            igual("Puntaje", 200, e.c.getPuntaje());
            igual("Pasa a REGRESANDO", EstadoFantasma.REGRESANDO, e.f[0].getEstado());
            e.p.resolverColision(e.f[0]);
            igual("No duplica puntaje", 200, e.c.getPuntaje());
            igual("Sin perdida de vidas", 3, e.c.getVidas());
        });
        caso("Contacto real con los cuatro asustados cobra 800 mas el superpunto", () -> {
            Escenario e = pasillo(true, 5, 2, 50);
            for (Fantasma f : e.f) ubicar(f, e.mapa, new Posicion(5, 1));
            e.c.avanzarTurno(Direccion.IZQUIERDA);
            igual("50 + 4 x 200", 850, e.c.getPuntaje());
            igual("No pierde vida", 3, e.c.getVidas());
            estados("Los cuatro regresan", e.c, EstadoFantasma.REGRESANDO);
        });
        caso("Regresa a su propio origen, luego vuelve a perseguir", () -> {
            Escenario e = escenario(2, 3);
            Posicion origen = e.f[0].getPosicion();
            ubicar(e.f[0], e.mapa, new Posicion(7, 4));
            e.f[0].asustar(10);
            e.p.resolverColision(e.f[0]);
            e.c.avanzarTurno(null);
            posicion("Primer paso hacia origen", 6, 4, e.f[0].getPosicion());
            igual("Aun regresando", EstadoFantasma.REGRESANDO, e.f[0].getEstado());
            e.c.avanzarTurno();
            posicion("Llega al origen propio", origen, e.f[0].getPosicion());
            igual("Reactivado al llegar", EstadoFantasma.PERSIGUIENDO, e.f[0].getEstado());
            igual("Regresar no agrega puntos", 200, e.c.getPuntaje());
        });
        caso("REGRESANDO es inofensivo incluso al intercambiar celdas", () -> {
            Escenario e = escenario(5, 4);
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 4));
            e.f[0].asustar(10);
            e.f[0].serComido();
            e.c.avanzarTurno(Direccion.ABAJO);
            posicion("PacMan cruzo al fantasma", 6, 4, e.c.getPosicionPacMan());
            posicion("Fantasma llego al origen", 5, 4, e.f[0].getPosicion());
            igual("No pierde vidas", 3, e.c.getVidas());
            igual("No suma por el contacto", 0, e.c.getPuntaje());
            igual("Al llegar vuelve a perseguir", EstadoFantasma.PERSIGUIENDO, e.f[0].getEstado());
        });
        caso("Al volver a perseguir vuelve a ser peligroso", () -> {
            Escenario e = escenario(5, 4);
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 4));
            e.f[0].asustar(10);
            e.f[0].serComido();
            e.c.avanzarTurno(Direccion.ABAJO);
            e.c.avanzarTurno(null);
            igual("Nuevo contacto como perseguidor", 2, e.c.getVidas());
        });
        caso("Solo se puede comer a un fantasma asustado", () -> {
            Fantasma f = new Fantasma(new Posicion(5, 4));
            f.serComido();
            igual("Perseguidor no cambia", EstadoFantasma.PERSIGUIENDO, f.getEstado());
            f.asustar(10);
            f.serComido();
            f.asustar(10);
            igual("Regresando ignora susto", EstadoFantasma.REGRESANDO, f.getEstado());
        });
    }

    private static void muertePacMan() {
        grupo(7, "Colisiones y reinicio tras perder vida");
        caso("Misma celda de llegada: pierde una vida y reinicia posiciones", () -> {
            Escenario e = escenario(6, 3);
            Posicion[] origenes = e.c.getPosicionesFantasmas();
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 5));
            e.c.avanzarTurno(Direccion.DERECHA);
            igual("Una vida menos", 2, e.c.getVidas());
            posicion("PacMan al inicio", 6, 3, e.c.getPosicionPacMan());
            posiciones("Fantasmas a sus inicios propios", origenes, e.c.getPosicionesFantasmas());
            estados("Todos persiguen", e.c, EstadoFantasma.PERSIGUIENDO);
        });
        caso("Intercambio de celdas tambien detecta contacto", () -> {
            Escenario e = escenario(6, 3);
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 4));
            e.c.avanzarTurno(Direccion.DERECHA);
            igual("Cruce peligroso", 2, e.c.getVidas());
        });
        caso("Entrar a la celda que deja un fantasma no es contacto si no hay cruce", () -> {
            Escenario e = escenario(6, 3);
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 4));
            List<Posicion> destinos = Arrays.asList(e.c.getPosicionesFantasmas());
            destinos.set(0, new Posicion(5, 4));
            comprobar(e.p.detectarColision(new Posicion(6, 4), destinos).isEmpty(),
                "Misma celda anterior sin intercambio no debe dar falso positivo");
            // Verifica tambien que el detector no mueva personajes por su cuenta.
            posicion("PacMan sin mover", 6, 3, e.c.getPosicionPacMan());
            posicion("Fantasma sin mover", 6, 4, e.f[0].getPosicion());
        });
        caso("Pasar por celdas vecinas sin coincidir no causa una colision", () -> {
            Escenario e = escenario(6, 3);
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(5, 5));
            e.c.avanzarTurno(Direccion.DERECHA);
            posicion("PacMan avanza", 6, 4, e.c.getPosicionPacMan());
            igual("Vecinos no pierden vidas", 3, e.c.getVidas());
        });
        caso("Contacto sin mover a PacMan y varios perseguidores descuentan una sola vida", () -> {
            Escenario e = escenario(6, 3);
            for (Fantasma f : e.f) ubicar(f, e.mapa, new Posicion(6, 4));
            e.c.avanzarTurno(null);
            igual("Cuatro contactos, una vida", 2, e.c.getVidas());
            igual("Sigue en curso", EstadoPartida.EN_CURSO, e.c.getEstado());
        });
        caso("Contacto mixto cobra al asustado antes de perder vida, en ambos ordenes", () -> {
            for (int indiceAsustado : new int[] {0, 1}) {
                Escenario e = pasillo(true, 5, 2, 10);
                ubicar(e.f[1], e.mapa, new Posicion(5, 1));
                e.f[indiceAsustado].asustar(10);
                e.c.avanzarTurno(Direccion.IZQUIERDA);
                igual("Punto y fantasma; orden " + indiceAsustado, 210, e.c.getPuntaje());
                igual("Una sola vida; orden " + indiceAsustado, 2, e.c.getVidas());
                estados("Susto cancelado por muerte", e.c, EstadoFantasma.PERSIGUIENDO);
            }
        });
        caso("Perder vida conserva puntaje y puntos consumidos", () -> {
            Escenario e = escenario(6, 3, true, 6, 4, 10);
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 5));
            e.c.avanzarTurno(Direccion.DERECHA);
            igual("Punto cobrado antes de morir", 10, e.c.getPuntaje());
            igual("Punto no reaparece", ' ', e.c.getMapa()[6][4]);
            igual("Otros puntos siguen presentes", '.', e.c.getMapa()[9][1]);
            igual("Se perdio una vida", 2, e.c.getVidas());
        });
        caso("El reinicio cancela susto y regreso; PacMan vuelve hacia la derecha", () -> {
            Escenario e = escenario(2, 3);
            e.c.avanzarTurno(Direccion.IZQUIERDA);
            e.f[0].asustar(10);
            e.f[1].asustar(10);
            e.f[1].serComido();
            e.p.perderVida();
            estados("Estados reiniciados", e.c, EstadoFantasma.PERSIGUIENDO);
            e.c.avanzarTurno();
            posicion("Reaparece mirando derecha", 2, 4, e.c.getPosicionPacMan());
            e.c.moverPacMan(null);
            e.c.actualizar(20);
            estados("No queda susto pendiente", e.c, EstadoFantasma.PERSIGUIENDO);
        });
        caso("Sin contacto no pierde vidas", () -> {
            Escenario e = escenario(2, 3);
            for (int i = 0; i < 10; i++) e.c.avanzarTurno(null);
            igual("Vidas intactas", 3, e.c.getVidas());
            igual("Puntaje intacto", 0, e.c.getPuntaje());
        });
    }

    private static void vidas() {
        grupo(8, "Vidas y derrota");
        caso("Comienza con tres vidas y cero puntos", () -> {
            ControladorJuego c = new ControladorJuego();
            igual("Tres vidas", 3, c.getVidas());
            igual("Cero puntos", 0, c.getPuntaje());
            igual("En curso", EstadoPartida.EN_CURSO, c.getEstado());
        });
        caso("Tres colisiones reales llevan de 3 a 0 y a DERROTA", () -> {
            Escenario e = escenario(6, 3);
            for (int perdida = 1; perdida <= 3; perdida++) {
                estacionar(e);
                ubicar(e.f[0], e.mapa, new Posicion(6, 5));
                e.c.avanzarTurno(Direccion.DERECHA);
                igual("Vidas tras contacto " + perdida, 3 - perdida, e.c.getVidas());
                igual("Estado tras contacto " + perdida,
                    perdida == 3 ? EstadoPartida.DERROTA : EstadoPartida.EN_CURSO, e.c.getEstado());
            }
        });
        caso("La derrota bloquea movimiento, consumo, tiempo y pausas", () -> {
            Escenario e = derrotada();
            String antes = foto(e.c);
            e.c.avanzarTurno(Direccion.IZQUIERDA);
            e.c.actualizar(20);
            e.c.pausar();
            e.c.reanudar();
            e.c.alternarPausa();
            igual("Partida finalizada inmutable desde el controlador", antes, foto(e.c));
        });
        caso("No puede bajar de cero vidas", () -> {
            Escenario e = derrotada();
            for (int i = 0; i < 5; i++) e.p.perderVida();
            igual("Minimo cero", 0, e.c.getVidas());
            igual("Sigue en derrota", EstadoPartida.DERROTA, e.c.getEstado());
        });
    }

    private static void victoria() {
        grupo(9, "Victoria");
        caso("No gana mientras quede un punto comun", () -> {
            Escenario e = escenario(2, 3, false, 2, 4, 50, 2, 6, 10);
            e.c.avanzarTurno();
            igual("Queda comun", EstadoPartida.EN_CURSO, e.c.getEstado());
            comprobar(!e.p.comprobarVictoria(), "No cumple victoria");
            e.c.avanzarTurno();
            e.c.avanzarTurno();
            igual("Al consumir el comun gana", EstadoPartida.VICTORIA, e.c.getEstado());
            igual("Ambos puntos sumados", 60, e.c.getPuntaje());
        });
        caso("No gana mientras quede un superpunto; gana al consumirlo", () -> {
            Escenario e = escenario(2, 3, false, 2, 4, 10, 2, 5, 50);
            e.c.avanzarTurno();
            igual("Queda superpunto", EstadoPartida.EN_CURSO, e.c.getEstado());
            e.c.avanzarTurno();
            igual("Victoria", EstadoPartida.VICTORIA, e.c.getEstado());
            igual("Total", 60, e.c.getPuntaje());
            posicion("PacMan queda en el ultimo punto", 2, 5, e.c.getPosicionPacMan());
            comprobar(!e.mapa.quedanPuntos(), "Mapa sin puntos");
        });
        caso("Ultimo punto tiene prioridad sobre contacto peligroso", () -> {
            Escenario e = escenario(6, 3, false, 6, 4, 10);
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 4));
            e.c.avanzarTurno(Direccion.DERECHA);
            igual("Victoria antes de muerte", EstadoPartida.VICTORIA, e.c.getEstado());
            igual("Conserva vidas", 3, e.c.getVidas());
            igual("Cobra ultimo punto", 10, e.c.getPuntaje());
        });
        caso("Un mapa sin puntos termina en victoria al actualizar", () -> {
            Escenario e = escenario(2, 3, false);
            comprobar(e.p.comprobarVictoria(), "Sin puntos pendientes");
            e.c.avanzarTurno();
            igual("Victoria", EstadoPartida.VICTORIA, e.c.getEstado());
            igual("Sin puntos inventados", 0, e.c.getPuntaje());
        });
        caso("Tras victoria se detienen modelo y controlador", () -> {
            Escenario e = ganada();
            String antes = foto(e.c);
            e.c.avanzarTurno(Direccion.ABAJO);
            e.c.actualizar(10);
            e.p.actualizar(10);
            e.c.pausar();
            e.c.reanudar();
            e.p.perderVida();
            igual("Estado final conservado", antes, foto(e.c));
        });
    }

    private static void controlJuego() {
        grupo(10, "Pausa, reanudacion y nueva partida");
        caso("Pausa congela posiciones, puntaje, mapa, estados, ticks y reloj", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 50, 2, 5, 10);
            e.c.avanzarTurno();
            e.c.pausar();
            igual("Pausada", EstadoPartida.PAUSADA, e.c.getEstado());
            String antes = foto(e.c);
            e.c.avanzarTurno(Direccion.IZQUIERDA);
            e.c.actualizar(100);
            e.p.actualizar(100);
            e.c.pausar();
            igual("Nada avanza en pausa", antes, foto(e.c));
            e.c.reanudar();
            e.c.avanzarTurno();
            posicion("Conserva direccion anterior a la pausa", 2, 5, e.c.getPosicionPacMan());
            igual("Consume despues de reanudar", 60, e.c.getPuntaje());
            estados("Pausa no consumio susto", e.c, EstadoFantasma.ASUSTADO);
            e.c.moverPacMan(null);
            e.c.actualizar(10 - TICK);
            estados("Susto vence contando solo tiempo activo", e.c, EstadoFantasma.PERSIGUIENDO);
        });
        caso("Alternar pausa y reanudar repetidamente no avanza turnos", () -> {
            Escenario e = escenario(2, 3);
            e.c.reanudar();
            igual("Reanudar en curso es inocuo", EstadoPartida.EN_CURSO, e.c.getEstado());
            for (int i = 0; i < 3; i++) {
                e.c.alternarPausa();
                igual("Pausa", EstadoPartida.PAUSADA, e.c.getEstado());
                e.c.alternarPausa();
                igual("Reanudacion", EstadoPartida.EN_CURSO, e.c.getEstado());
            }
            igual("Sin ticks extras", 0L, e.c.getTicks());
            cerca("Sin tiempo extra", 0, e.c.getTiempoSimulado());
        });
        caso("Rechaza una nueva partida en curso y en pausa sin perder progreso", () -> {
            Escenario e = escenario(2, 3, true, 2, 4, 10);
            e.c.avanzarTurno();
            for (boolean pausar : new boolean[] {false, true}) {
                if (pausar) e.c.pausar();
                String antes = foto(e.c);
                comprobar(!e.c.nuevaPartida(), "Reinicio rechazado");
                igual("Conserva partida", antes, foto(e.c));
            }
        });
        caso("Nueva partida tras victoria restaura todos los valores iniciales", () -> {
            Escenario e = ganada();
            reiniciarYComprobar(e);
        });
        caso("Nueva partida tras derrota restaura todos los valores iniciales", () -> {
            Escenario e = derrotada();
            reiniciarYComprobar(e);
        });
        caso("Reiniciar crea un mapa independiente del anterior", () -> {
            Escenario e = ganada();
            comprobar(e.c.nuevaPartida(), "Reinicio aceptado");
            String nueva = foto(e.c);
            e.p.consumir(new SuperPunto());
            for (Fantasma f : e.f) f.asustar(99);
            e.mapa.obtenerCelda(new Posicion(2, 5)).retirarPunto();
            igual("Modificar partida anterior no afecta nueva", nueva, foto(e.c));
        });
        caso("Consola: entrada invalida, pausa, reinicio rechazado, Q y fin de entrada", () -> {
            String salida = consola("x\np\nd\nn\np\n\nQ\n");
            contiene("Comando invalido", salida, "Comando invalido.");
            contiene("Pausa visible", salida, "PAC-MAN | PAUSADA", "Partida pausada.");
            contiene("Reinicio rechazado", salida, "Solo podes iniciar otra partida");
            contiene("Solo avanza el Enter tras reanudar", salida, "Tick: 1 | Tiempo: 0.042");
            comprobar(!salida.contains("Tick: 2"), "Otros comandos no cuentan ticks");
            contiene("Salida con Q mayuscula", salida, "Juego cerrado.");
            contiene("Fin de entrada tambien cierra", consola(""), "Juego cerrado.");
        });
    }

    // Preparacion de escenarios: solo los metodos siguientes conocen los mapas.

    private static class Escenario {
        final Laberinto mapa;
        final Partida p;
        final ControladorJuego c;
        final Fantasma[] f;

        Escenario(Laberinto mapa, Posicion inicio) {
            this.mapa = mapa;
            p = new Partida(mapa, inicio);
            c = new ControladorJuego(p);
            try {
                Field campo = Partida.class.getDeclaredField("fantasmas");
                campo.setAccessible(true);
                f = (Fantasma[]) campo.get(p);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("No se pudieron preparar los fantasmas", ex);
            }
        }
    }

    private static Escenario escenario(int fila, int columna) {
        return escenario(fila, columna, true);
    }

    // Cada terna indica fila, columna y valor (10 comun / 50 superpunto).
    private static Escenario escenario(int fila, int columna, boolean reserva, int... puntos) {
        return crear(BASE, fila, columna, reserva, puntos);
    }

    private static Escenario crear(String[] plano, int fila, int columna,
            boolean reserva, int... puntos) {
        Celda[][] celdas = celdas(plano);
        if (reserva) celdas[9][1] = new Celda(false, new PuntoComun(), 9, 1);
        if (puntos.length % 3 != 0) throw new IllegalArgumentException("Se requieren ternas");
        for (int i = 0; i < puntos.length; i += 3) {
            int f = puntos[i];
            int c = puntos[i + 1];
            if (celdas[f][c].esPared()) throw new IllegalArgumentException("Punto en pared");
            if (puntos[i + 2] != 10 && puntos[i + 2] != 50) {
                throw new IllegalArgumentException("Valor de punto invalido");
            }
            celdas[f][c] = new Celda(false,
                puntos[i + 2] == 50 ? new SuperPunto() : new PuntoComun(), f, c);
        }
        return new Escenario(new Laberinto(celdas), new Posicion(fila, columna));
    }

    private static Celda[][] celdas(String[] plano) {
        Celda[][] celdas = new Celda[plano.length][plano[0].length()];
        for (int f = 0; f < plano.length; f++) {
            for (int c = 0; c < plano[f].length(); c++) {
                celdas[f][c] = new Celda(plano[f].charAt(c) == '#', null, f, c);
            }
        }
        return celdas;
    }

    private static Escenario pasillo(boolean reserva, int... puntos) {
        String[] plano = BASE.clone();
        plano[6] = "#############";
        plano[7] = "#############";
        Escenario e = crear(plano, 5, 3, reserva, puntos);
        for (Fantasma f : e.f) ubicar(f, e.mapa, new Posicion(5, 11));
        ubicar(e.f[0], e.mapa, new Posicion(5, 1));
        return e;
    }

    private static void estacionar(Escenario e) {
        for (Fantasma f : e.f) ubicar(f, e.mapa, new Posicion(7, 11));
    }

    private static void ubicar(Personaje personaje, Laberinto mapa, Posicion destino) {
        List<Posicion> camino = mapa.caminoMasCorto(personaje.getPosicion(), destino);
        if (camino.isEmpty()) throw new IllegalArgumentException("Preparacion sin camino al destino");
        for (int i = 1; i < camino.size(); i++) {
            Posicion antes = personaje.getPosicion();
            Posicion siguiente = camino.get(i);
            Direccion direccion = siguiente.fila < antes.fila ? Direccion.ARRIBA
                : siguiente.fila > antes.fila ? Direccion.ABAJO
                : siguiente.columna < antes.columna ? Direccion.IZQUIERDA : Direccion.DERECHA;
            personaje.mover(direccion, mapa);
        }
        posicion("Preparacion del personaje", destino, personaje.getPosicion());
    }

    private static Escenario ganada() {
        Escenario e = escenario(2, 3, false, 2, 4, 10);
        e.c.avanzarTurno();
        igual("Preparacion: victoria", EstadoPartida.VICTORIA, e.c.getEstado());
        return e;
    }

    private static Escenario derrotada() {
        Escenario e = escenario(6, 3, true, 6, 4, 10);
        for (int i = 0; i < 3; i++) {
            estacionar(e);
            ubicar(e.f[0], e.mapa, new Posicion(6, 5));
            e.c.avanzarTurno(Direccion.DERECHA);
        }
        igual("Preparacion: derrota", EstadoPartida.DERROTA, e.c.getEstado());
        return e;
    }

    private static void reiniciarYComprobar(Escenario e) {
        String anterior = foto(new ControladorJuego(e.p));
        comprobar(e.c.nuevaPartida(), "Reinicio aceptado");
        igual("Igual a una partida recien creada", foto(new ControladorJuego()), foto(e.c));
        igual("Partida anterior conservada", anterior, foto(new ControladorJuego(e.p)));
        comprobar(!e.c.nuevaPartida(), "Segundo reinicio rechazado mientras esta en curso");
        e.c.avanzarTurno();
        posicion("Nueva partida puede jugarse hacia la derecha", 13, 8, e.c.getPosicionPacMan());
        igual("Vuelve a sumar", 10, e.c.getPuntaje());
        igual("Reloj reiniciado y un nuevo tick", 1L, e.c.getTicks());
        cerca("Tiempo de nueva partida", TICK, e.c.getTiempoSimulado());
    }

    private static String consola(String comandos) {
        InputStream entradaOriginal = System.in;
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream captura = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(comandos.getBytes(StandardCharsets.UTF_8)));
            System.setOut(captura);
            JuegoConsola.main(new String[0]);
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(salidaOriginal);
        }
    }

    private static void posicionEnPantalla(String pantalla, int fila, int columna) {
        List<String> tablero = pantalla.lines()
            .filter(linea -> linea.startsWith("# ")).toList();
        igual("Quince filas en consola", 15, tablero.size());
        igual("PacMan visible en " + fila + "," + columna, 'P', tablero.get(fila).charAt(columna * 2));
    }

    private static String foto(ControladorJuego c) {
        return c.getEstado() + "|" + c.getVidas() + "|" + c.getPuntaje()
            + "|" + texto(c.getPosicionPacMan()) + "|" + texto(c.getPosicionesFantasmas())
            + "|" + Arrays.toString(c.getEstadosFantasmas()) + "|" + Arrays.deepToString(c.getMapa())
            + "|" + c.getTicks() + "|" + c.getTiempoSimulado();
    }

    private static String texto(Posicion p) { return "(" + p.fila + "," + p.columna + ")"; }

    private static String texto(Posicion[] posiciones) {
        return Arrays.stream(posiciones).map(TestRequerimientos::texto).toList().toString();
    }

    private static Posicion vecino(Posicion p, Direccion d) {
        return switch (d) {
            case ARRIBA -> new Posicion(p.fila - 1, p.columna);
            case ABAJO -> new Posicion(p.fila + 1, p.columna);
            case IZQUIERDA -> new Posicion(p.fila, p.columna - 1);
            case DERECHA -> new Posicion(p.fila, p.columna + 1);
        };
    }

    private static int distancia(Posicion a, Posicion b) {
        return Math.abs(a.fila - b.fila) + Math.abs(a.columna - b.columna);
    }

    // Comprobaciones explicitas: funcionan sin habilitar las assertions de Java.

    private static void grupo(int numero, String nombre) {
        requisito = numero;
        System.out.printf("%nR%02d - %s%n", numero, nombre);
    }

    private static void caso(String nombre, Runnable prueba) {
        casos++;
        ejecutados[requisito]++;
        try {
            prueba.run();
            System.out.println("[OK] " + nombre);
        } catch (RuntimeException | AssertionError ex) {
            fallidos[requisito]++;
            System.out.println("[FALLO] " + nombre + "\n        " + ex);
            for (StackTraceElement origen : ex.getStackTrace()) {
                if (origen.getClassName().equals(TestRequerimientos.class.getName())) {
                    System.out.println("        " + origen);
                    break;
                }
            }
        }
    }

    private static void comprobar(boolean condicion, String descripcion) {
        comprobaciones++;
        if (!condicion) throw new AssertionError(descripcion);
    }

    private static void igual(String descripcion, Object esperado, Object obtenido) {
        comprobaciones++;
        if (!Objects.equals(esperado, obtenido)) {
            throw new AssertionError(descripcion + ": esperado=" + esperado + "; obtenido=" + obtenido);
        }
    }

    private static void cerca(String descripcion, double esperado, double obtenido) {
        comprobar(Double.isFinite(obtenido) && Math.abs(esperado - obtenido) <= 1e-9,
            descripcion + ": esperado=" + esperado + "; obtenido=" + obtenido);
    }

    private static void posicion(String descripcion, int fila, int columna, Posicion obtenida) {
        posicion(descripcion, new Posicion(fila, columna), obtenida);
    }

    private static void posicion(String descripcion, Posicion esperada, Posicion obtenida) {
        igual(descripcion, texto(esperada), texto(obtenida));
    }

    private static void posiciones(String descripcion, Posicion[] esperadas, Posicion[] obtenidas) {
        igual(descripcion, texto(esperadas), texto(obtenidas));
    }

    private static void estados(String descripcion, ControladorJuego c, EstadoFantasma esperado) {
        EstadoFantasma[] estados = c.getEstadosFantasmas();
        igual(descripcion + ": cantidad", 4, estados.length);
        for (int i = 0; i < estados.length; i++) igual(descripcion + ": fantasma " + i, esperado, estados[i]);
    }

    private static void contiene(String descripcion, String salida, String... fragmentos) {
        for (String fragmento : fragmentos) {
            comprobar(salida.contains(fragmento), descripcion + ": no aparece '" + fragmento + "'");
        }
    }
}
