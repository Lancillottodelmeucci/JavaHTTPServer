/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testgroup.prototipo;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author giova
 */
public class InterrogazioneDB {
    private final String DRIVER="com.mysql.cj.jdbc.Driver";
    private String url_db="jdbc:mysql://localhost:3306/webserver_db?serverTimezone=Europe/Rome";
    private String query;
    private Connection conn;
    private ResultSet result;
    public InterrogazioneDB() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER);
        conn=DriverManager.getConnection(url_db, "root", "01-2W-Lq");
    }
    public void setQuery(String q){
        if(q.toUpperCase().startsWith("SELECT")){
            query=q;
        }
        else{
            query="select \"Formato della Query non valido\";";
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
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        String DRIVER="com.mysql.cj.jdbc.Driver";
        Class.forName(DRIVER);
        String url_db="jdbc:mysql://localhost:3306/webserver_db?serverTimezone=Europe/Rome";
        String query="select nome, cognome from persone";
        Connection conn=DriverManager.getConnection(url_db, "root", "01-2W-Lq");
        Statement stat=conn.createStatement();
        ResultSet res=stat.executeQuery(query);
        ArrayList<Nominativo> nomi=new ArrayList<>();
        while (res.next()) {
            // System.out.println("Nome: "+res.getNString(1)+"\tCognome: "+res.getString(2));
            nomi.add(new Nominativo(res.getString(1), res.getString(2)));
        }
        conn.close();
        Elenco el=new Elenco(nomi);
        //creo il mapper xml
        XmlMapper xmlMapper = new XmlMapper();
        //creo l'array di byte contenente l'xml della classe
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        xmlMapper.writeValue(byteArray, el);
        //trasformo l'array di byte in stringa
        String arrayXML=byteArray.toString();
        System.out.println(arrayXML);
            File F=new File("");
            String PATH=F.getAbsolutePath();
            File WEB_ROOT = new File(PATH+"\\src\\main\\java\\testgroup\\websources");
            File fXML=new File(WEB_ROOT+"\\elenco.xml");
        if(fXML.exists()){
            fXML.delete();//usando il write potrei evitarlo e crearlo solo se non exists
        }
        fXML.createNewFile();
        FileWriter fw=new FileWriter(fXML);
        fw.write(arrayXML);
        fw.close();
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
