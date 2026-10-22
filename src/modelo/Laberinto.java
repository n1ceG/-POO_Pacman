package modelo;

import extra.Direccion;
import extra.Posicion;
import java.util.List;

public class Laberinto {

    private Celda[][] celdas;

    public boolean esTransitable(Posicion posicion)
    { return false; }

    public List<Direccion> direccionesDisponibles(Posicion posicion)
    { return null; }

    public List<Posicion> caminoMasCorto(
        Posicion origen,
        Posicion destino
    )
    {
        return null;
    }

    public boolean quedanPuntos()
    { return false; }

    public Posicion centro()
    { return null; }

    public Celda obtenerCelda(Posicion posicion)
    { return null; }
}
