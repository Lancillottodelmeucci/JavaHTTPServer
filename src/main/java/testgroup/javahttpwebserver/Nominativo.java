package testgroup.javahttpwebserver;

/**
 * La classe che contiene i dati per gestire i nominativi delle persone
 * @author Giovanni Ciaranfi
 */
public class Nominativo {
    private String nome;
    private String cognome;

    public Nominativo(String nome, String cognome) {
        this.nome = nome;
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    
}
