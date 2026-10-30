# Diagramas del código de Pac-Man

Elaborados con asistencia de IA a pedido de Juan el **30/10/2026**, a partir del código de `develop`, commit `0d8309096b4f707c340a7b17210a8611dadffe6e`. No son material original de la cátedra. Los diagramas anteriores se conservan intactos.

## Cómo verlos

- **StarUML:** abrir `Pacman-desde-codigo.mdj`. Contiene un diagrama de clases y diez de secuencia, todos editables. En **Model Explorer**, desplegar `Aplicación` o `Secuencias por requerimiento`. Abrir cada diagrama con doble clic en su **icono**. Usar el zoom para recorrerlo.
- **Navegador:** abrir `Ver-diagramas.html`. Elegir un diagrama en el menú y usar `+`, `−`, `100%` o `Ajustar ancho`. Funciona sin conexión y permite descargar cada SVG.
- **Imágenes vectoriales:** `svg/` contiene los once diagramas. Conservan la nitidez al ampliar.

`Pacman-diagramas.zip` reúne los entregables, esta guía y los archivos usados para generarlos.

## Alcance

El diagrama de clases incluye las **15 clases y enumeraciones de la aplicación**: modelo, controlador, consola y tipos auxiliares. Muestra todos sus atributos y operaciones declarados, constructores, visibilidades, herencia, constantes, métodos estáticos y abstractos. Las operaciones heredadas se consultan en la clase base. Las pruebas y los tipos de la biblioteca Java quedan fuera de este modelo de la aplicación.

Las asociaciones representan referencias almacenadas. La composición se usa para objetos creados y conservados por su propietario; no se presupone exclusividad para el laberinto o la partida inyectados por constructor. Los usos adicionales de tipos se leen en las firmas. `Celda()` permite una posición nula; `Personaje` conserva dos posiciones propias.

Los números **R01–R10 corresponden al [archivo de requerimientos](../../Requerimientos%20TPO%20PacMan%201.md)**, no a las tarjetas N de Trello.

| Diagrama | Requerimiento | Métodos principales del código |
| --- | --- | --- |
| R01 | Movimiento | `JuegoConsola.procesar`, `ControladorJuego.avanzarTurno`, `Partida.actualizar`, `PacMan.elegirDireccion/mover` |
| R02 | Paredes | `Personaje.calcularProximaPosicion`, `Laberinto.esTransitable/obtenerCelda`, `Celda.esPared` |
| R03 | Puntaje | `Celda.retirarPunto`, `Partida.consumir`, `PuntoComun.valor`, `SuperPunto.valor` |
| R04 | Enemigos | `Fantasma.elegirDireccion/mover`, `Laberinto.caminoMasCorto`, `Partida.detectarColision` |
| R05 | Superpunto | `Partida.consumir`, `Fantasma.asustar/elegirDireccion/actualizarEstado` |
| R06 | Consumo de fantasmas | `Partida.resolverColision`, `Fantasma.serComido/elegirDireccion/actualizarEstado` |
| R07 | Muerte de PacMan | `Partida.detectarColision/actualizar/perderVida`, `volverAlInicio` en los personajes |
| R08 | Vidas y derrota | Constructores de `Partida`, `Partida.perderVida`, `ControladorJuego.actualizar` |
| R09 | Victoria | `Partida.actualizar/comprobarVictoria`, `Laberinto.quedanPuntos`, `Celda.tienePunto` |
| R10 | Control del juego | `ControladorJuego.alternarPausa/nuevaPartida`, `Partida.pausar/reanudar` |

Las flechas son llamadas o retornos. Las notas son asignaciones internas, precondiciones o partes omitidas del mismo tick. `alt` indica alternativas; `opt`, una condición; `loop`, una repetición. Las secuencias enfocan cada requerimiento y no duplican todo `actualizar()` diez veces. Se omiten las barras de activación para facilitar la lectura.

## Comportamientos que se respetaron al diagramar

- Los puntos se retiran y suman antes del movimiento y de resolver los contactos.
- Comer el último punto produce victoria antes de calcular movimientos de fantasmas y colisiones.
- La colisión compara próximas posiciones e intercambios de celdas, usando las posiciones actuales antes de mover.
- Primero se cobran los fantasmas asustados tocados y después se descuenta, como máximo, una vida por tick.
- Un fantasma `REGRESANDO` no daña ni vuelve a dar puntos.
- El susto recién renovado no descuenta tiempo en ese tick. En los siguientes, las colisiones se resuelven antes de actualizar el temporizador.
- La persecución usa BFS en cada tick, con desempate según el orden del enum `Direccion`. El asustado elige entre todas las salidas transitables.
- **El código actual crea los fantasmas en la misma fila y distintas columnas:** están alineados horizontalmente. El diagrama R08 refleja el constructor existente.
- Pausar congela el movimiento, los temporizadores y los contadores. Una partida nueva solo se acepta después de victoria o derrota.

## Fuentes y regeneración

Las clases se extrajeron de [src](../../src/). Las secuencias se trazaron leyendo esos métodos y se conservan en `secuencias.py`. No se dedujeron de los diagramas viejos. `fuentes-y-validacion.json` identifica los archivos originales mediante SHA-256 y registra el commit base.

Para regenerar desde esta carpeta, dentro del proyecto, se necesita **Python 3.10 o superior**, sin paquetes externos:

```powershell
python generar.py
```

El generador reconstruye el MDJ, el HTML, los SVG, el registro de fuentes y el ZIP. Verifica identificadores únicos, referencias, las 15 clases/enumeraciones, diez secuencias y que las llamadas a clases propias correspondan a métodos existentes, incluyendo herencia y sobrecargas. Las posiciones y recorridos de las secuencias se mantienen manualmente en `secuencias.py`; si cambia el comportamiento Java, hay que revisar también ese archivo. La regeneración sobrescribe estos entregables generados: guardar las ediciones manuales de StarUML con otro nombre.

El ZIP se puede consultar en cualquier PC sin el código fuente. Para **regenerar** los diagramas hace falta conservar esta carpeta dentro del proyecto junto con `src/`.
