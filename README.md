# Pac-Man - preparación para la fase B

Estructura preparada con asistencia de IA a pedido de Juan el 22/10/2026. Se replicó el formato de `Partida.java`, preparado por Juan: paquetes, imports, atributos, firmas de métodos y cuerpos vacíos o retornos provisionales. La lógica será desarrollada por Juan y su grupo.

## Alcance y fuentes

- La página 2 de [la consigna docente](TPO_PacMan_Enunciado.pdf) define la fase B como "Código del negocio funcionando con controlador y test". La interfaz gráfica corresponde a la entrega final.
- Juan confirmó que [DiagramaClasesTPO_1.mdj](StarUML/DiagramaClasesTPO_1.mdj) es la referencia previa a la devolución docente. El diseño sigue pendiente de correcciones; esta preparación no implica aprobación.
- [MuevoObjetos](../MuevoObjetos/) es el ejemplo que Juan identifica como aportado por el profesor. Se toma su separación entre modelo, controlador y prueba de consola. Su configuración utiliza Java 17.
- Según lo explicado por Juan, las pruebas deberán verificar que los valores y resultados sean razonables, siguiendo la idea de `MuevoObjetos/src/test/TestControlador.java`.

## Abrir en VS Code

1. Usar **Archivo > Abrir carpeta** y seleccionar esta carpeta `TPO-Pacman`. Así se aplica su configuración propia, sin mezclar los ejercicios de otras carpetas.
2. Tener instalado un JDK y la extensión **Extension Pack for Java**, recomendada en `.vscode/extensions.json`.
3. Abrir los archivos de `src`. VS Code utiliza `src` como raíz de los paquetes y `bin` para los compilados.

No se requieren bibliotecas externas, Maven ni Gradle. El esqueleto se verificó con JDK 26.0.2 compilando con `--release 17`, compatible con la versión declarada por el ejemplo. La configuración no fija una ruta de JDK exclusiva de esta PC.

## Archivos para desarrollar

```text
src/
  controlador/
    ControladorJuego.java
  extra/
    Direccion.java
    EstadoFantasma.java
    EstadoPartida.java
    Posicion.java
  modelo/
    Celda.java
    Fantasma.java
    Laberinto.java
    PacMan.java
    Partida.java
    Personaje.java
    Punto.java
    PuntoComun.java
    SuperPunto.java
  test/
    TestControlador.java
```

`modelo` contiene el negocio; `controlador`, su coordinación; `extra`, posiciones y enumeraciones; y `test`, la futura prueba del controlador. Se normalizó el nombre previo `controller` a `controlador`, como en el ejemplo.

`Personaje` y `Punto` son abstractas. `PacMan` y `Fantasma` extienden `Personaje`; `PuntoComun` y `SuperPunto` extienden `Punto`. Las tres enumeraciones incluyen los valores declarados en el StarUML. El archivo contiene tanto clases como enumeraciones con esos nombres: se preparó un único `enum` Java por nombre.

Los atributos y las firmas del resto de las clases siguen `StarUML/DiagramaClasesTPO_1.mdj`, convirtiendo `bool` a `boolean`. Se mantuvo intacto el archivo `Partida.java` de Juan. No se agregaron constructores ni atributos derivados de asociaciones que no estuvieran declarados como atributos en el modelo. `Punto.valor()` es abstracto; sus implementaciones y los demás métodos con retorno usan `0`, `false` o `null` únicamente para compilar. Incluso los getters quedan pendientes de implementar. Los métodos `void` y el `main` de `TestControlador` están vacíos.

Las clases de `views` se definirán si hacen falta para transportar datos. No se anticiparon clases de interfaz gráfica ni conversiones a views. Se preservaron los diagramas, los requerimientos y el ejemplo docente.

## Compilar y ejecutar el esqueleto

Desde una terminal PowerShell abierta en esta carpeta, con `javac` disponible:

```powershell
$fuentesTpo = @(Get-ChildItem -LiteralPath .\src -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -d bin $fuentesTpo
```

El esqueleto compila. `TestControlador` incluye un `main` vacío: ejecutarlo termina sin mostrar resultados ni verificar comportamientos. Para desarrollar las pruebas, completar ese método y ejecutarlo con **Run** en VS Code o con:

```powershell
java -cp bin test.TestControlador
```

Los archivos de `bin` se regeneran al compilar y no se versionan. Esta estructura no representa la fase B terminada.

## Revisión B02 — 27/10/2026

El código avanzó respecto del esqueleto descrito arriba. Consultar [contratos y auditoría de B02](docs/B02-contratos.md) y [verificación actual](docs/B02-verificacion.txt). El documento distingue implementaciones existentes, propuestas y decisiones pendientes de aceptación del grupo. La compilación fue exitosa; TestControlador conserva una expectativa antigua incompatible con el inicio hacia DERECHA.



## Checklist interactivo del TPO

Abrir [estado-tpo.html](docs/estado-tpo.html) para revisar y marcar los contratos de B02 por clase y comportamiento. Incluye relaciones entre objetos y exportacion/importacion de las marcas. Consultar [uso, fuentes y recuperacion](docs/estado-tpo-uso.md). Las marcas iniciales provienen de una lectura del codigo del 27/10/2026 y no equivalen a tarjetas aprobadas.
