package testgroup.javahttpwebserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * La classe per la gestione della connessione al database locale
 * @author Giovanni Ciaranfi
 */
public class InterrogazioneDB {
    private final String DRIVER;
    private String url_db;
    private String query;
    private Connection conn;
    private ResultSet result;
    private Settings settings;
    public InterrogazioneDB() throws ClassNotFoundException, SQLException {
        try {
            settings=new Settings().loadSettings();
        } catch (IOException e) {
            settings=Settings.defaultSettings();
        }
        DRIVER=settings.DRIVER;
        url_db="jdbc:mysql://"+settings.HOST+":"+settings.PORTA_DB+"/"+settings.DATABASE+"?serverTimezone="+settings.TIMEZONE;
        Class.forName(DRIVER);
        conn=DriverManager.getConnection(url_db, settings.USER, settings.PASSWORD);
    }
    public void setQuery(String q){
        if(q.toUpperCase().startsWith("SELECT")){
            query=q;
        }
        else{
            query="select \"Formato della Query non valido\";";
        }
        if(!query.endsWith(";")){
            query+=";";
        }
    }
    public ResultSet eseguiQuery() throws SQLException{
        Statement stat=conn.createStatement();
        result=stat.executeQuery(query);
        return result;
    }
    public ResultSet eseguiQuery(String q) throws SQLException{
        setQuery(q);
        Statement stat=conn.createStatement();
        result=stat.executeQuery(query);
        return result;
    }
    public void chiudi() throws SQLException{
        conn.close();
    }

    public void setUrl_db(String url_db) {
        this.url_db = url_db;//inserire match regexp
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public String getDRIVER() {
        return DRIVER;
    }

    public String getUrl_db() {
        return url_db;
    }

    public String getQuery() {
        return query;
    }

    public Connection getConn() {
        return conn;
    }

    public ResultSet getResult() {
        return result;
    }
}
