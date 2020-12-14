package testgroup.javahttpwebserver;

import java.util.ArrayList;

/**
 * La classe contenitore dei punti vendita disponibili
 * @author Giovanni Ciaranfi
 */
public class ListaRisultati {
    private int size;
    //il wrap lo rende più leggibile
    private ArrayList<PuntoVendita> listaRisultati;

    public void setSize(int size) {
        this.size = size;
    }

    public void setListaRisultati(ArrayList<PuntoVendita> listaRisultati) {
        this.listaRisultati = listaRisultati;
    }

    public int getSize() {
        return size;
    }

    public ArrayList<PuntoVendita> getListaRisultati() {
        return listaRisultati;
    }
}
