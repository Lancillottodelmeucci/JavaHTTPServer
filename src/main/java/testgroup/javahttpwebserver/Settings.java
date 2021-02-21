package testgroup.javahttpwebserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;

/**
 * La classe per gestire le impostazioni (relative alla connessione al DB)
 * @author Giovanni Ciaranfi
 */
public class Settings {
    static final String SOURCE_ROOT = "generalsources/";
    public String DRIVER;
    public String HOST;
    public int PORTA_DB;
    public int PORTA_SERVER;
    public String USER;
    public String PASSWORD;
    public String DATABASE;
    public String TIMEZONE;
    public static final void saveSettings(Settings sets) throws IOException{//non completamente implementato
        ObjectMapper objectMapper = new ObjectMapper();
        File set_file=new File("settings.json");
        set_file.createNewFile();
        objectMapper.writeValue(set_file, sets);
        System.out.println(set_file.getAbsolutePath());
    }
    /**
     * il metodo per caricare le impostazioni dal file
     * @return le impostazioni per la connessione al DB
     * @throws IOException in caso di errore nel caricamento del file
     */
    public final Settings loadSettings() throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        Settings ret;
        File sets=new File("settings.json");
        sets.createNewFile();
        InputStream fin=getClass().getClassLoader().getResourceAsStream(SOURCE_ROOT+"settings.json");
        FileUtils.copyInputStreamToFile(fin, sets);
        ret = objectMapper.readValue(sets, Settings.class);
        fin.close();
        return ret;
    }
    /**
     * metodo chiamato in caso di eccezione nel caricamento delle impostazioni 
     * di connessione sal database
     * @return le impostazioni per la connessione al DB
     */
    public static final Settings defaultSettings(){
        Settings ret=new Settings();
        ret.DATABASE="webserver_db";
        ret.DRIVER="com.mysql.cj.jdbc.Driver";
        ret.HOST="localhost";
        ret.PASSWORD="01-2W-Lq";
        ret.PORTA_DB=3306;
        ret.TIMEZONE="Europe/Rome";
        ret.USER="root";
        return ret;
    }
}
