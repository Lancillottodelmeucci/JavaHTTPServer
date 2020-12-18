package testgroup.javahttpwebserver;

import java.util.ArrayList;

/**
 * La classe contenitore dei nominativi
 * @author Giovanni Ciaranfi
 */
public class Elenco {
    private ArrayList<Nominativo> nominativi;

    public Elenco(ArrayList<Nominativo> nomi) {
        nominativi=nomi;
    }

    public ArrayList<Nominativo> getNominativi() {
        return nominativi;
    }

    public void setNominativi(ArrayList<Nominativo> nominativi) {
        this.nominativi = nominativi;
    }
    
}
