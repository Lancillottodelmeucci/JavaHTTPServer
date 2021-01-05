package testgroup.javahttpwebserver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * La classe per la gesione continua delle connessioni, tarmite il metodo main, 
 * e che crea un thread per ognuna permettendo la sua gestione con il metodo 
 * run
 * @author Giovanni Ciaranfi
 */

//ogni connessione da parte del client viene gestita da un thread
public class WebServer implements Runnable{
    static final File F=new File("");
    static final String PATH=F.getAbsolutePath();
    static final File WEB_ROOT = new File(PATH+"\\src\\main\\java\\testgroup\\websources");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final String FILE_MOVED="301.html";
    //la porta sulla quale il server è in ascolto
    static final int PORT = 3000;
    // VERBOSE mode
    static final boolean VERBOSE = true;
    //il socket del client che si connette
    private Socket clientSocket;
    /**
     * 
     * @param c 
     */
    public WebServer(Socket c) {
        clientSocket = c;
    }
    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
            try {
                ServerSocket serverConnect = new ServerSocket(PORT);
                System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
                //il ciclo continua finchè viene permessa l'esecuzione del server
                while (true) {
                    WebServer myServer = new WebServer(serverConnect.accept());
                    if (VERBOSE) {
                        System.out.println("Connecton opened. (" + new Date() + ")");
                    }
                    //viene creato il thread per gestire la connessione del client
                    Thread thread = new Thread(myServer);
                    thread.start();
                }
            }
            catch (IOException e) {
                System.err.println("Server Connection error : " + e.getMessage());
            }
    }
    /**
     * 
     */
    @Override
    public void run() {
        //il metodo gestisce la connessione di ogni singolo client
        BufferedReader inDalClient = null;
        PrintWriter outAlClient = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;
        try {
            //input stream dal client per la lettura delle richieste
            inDalClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //output stream (character) sul quale inviare gli headers
            outAlClient = new PrintWriter(clientSocket.getOutputStream());
            //output stream (binary) sul quale inviare i dati della risposta
            dataOut = new BufferedOutputStream(clientSocket.getOutputStream());
            //viene letta la prima riga della richiesta del client
            String input = inDalClient.readLine();
            //la richiesta viene parsata con la classe StringTokenizer
            StringTokenizer parse = new StringTokenizer(input);
            //viene preso il metodo HTTP richiesto dal client
            String method = parse.nextToken().toUpperCase();
            //viene preso il file che il client richiese
            fileRequested = parse.nextToken().toLowerCase();
            //sono supportati solo il GET e l'HEAD quindi viene fatto un controllo
            if (!method.equals("GET")  &&  !method.equals("HEAD")) {
                if (VERBOSE) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }
                //verrà restituito il file di non supportazione del metodo al client
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);//non genererà eccezioni
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                //viene letto il contenuto del file da inviare al client
                byte[] fileData = readFileData(file, fileLength);
                //vengono inviati i seguenti HTTP headers con i dati al cleint
                outAlClient.println("HTTP/1.1 501 Not Implemented");
                outAlClient.println("Server: Java HTTP Server from SSaurel : 1.0");
                outAlClient.println("Date: " + new Date());
                outAlClient.println("Content-type: " + contentMimeType);
                outAlClient.println("Content-length: " + fileLength);
                //linea vuota necessaria tra gli headers e il contnuto
                outAlClient.println();
                // flush character output stream buffer
                outAlClient.flush();
                //viene inviato il contenuto del file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            }
            else {
                //se il metodo è tra quelli supportati (GET O HEAD)
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }
                File file;
                if(fileRequested.equals("/punti-vendita.xml")){
                    file=fromJSONToXML();
                }
                else if(fileRequested.equals("/db/xml")||fileRequested.equals("/db/json")){
                    Elenco el=retriveElenco();
                    if(fileRequested.endsWith("xml")){
                        file=classToXML(el);
                        fileRequested+="elenco.xml";
                    }
                    else{
                        file=classToJSON(el);
                        fileRequested+="elenco.json";
                    }
                }
                else{
                    file = new File(WEB_ROOT, fileRequested);
                }
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);//modificare il modo in cui recupero il content type?
                //se il metodo è il GET viene ritornato il contenuto
                if (method.equals("GET")) {//////////////////////////////e se è HEAD che faccio?
                    byte[] fileData = readFileData(file, fileLength);
                    //vengono inviati gli headers HTTP
                    outAlClient.println("HTTP/1.1 200 OK");
                    outAlClient.println("Server: Java HTTP Server from SSaurel : 1.0");
                    outAlClient.println("Date: " + new Date());
                    outAlClient.println("Content-type: " + content);
                    outAlClient.println("Content-length: " + fileLength);
                    //linea vuota necessaria tra gli headers e il contnuto
                    outAlClient.println();
                    // flush character output stream buffer
                    outAlClient.flush(); 
                    //viene inviato il contenuto del file richiesto
                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }
                if (VERBOSE) {
                    System.out.println("File " + fileRequested + " of type " + content + " returned");
                }
            }
        }
        catch (FileNotFoundException fnfe) {
            try {
                String[] percorso=fileRequested.split("/");
                int numPercorsi=percorso.length;
                //prendo il nome dell'oggetto richiesto per vedere se potrebbe essere una cartella
                String oggetto=percorso[numPercorsi-1];
                if(oggetto.lastIndexOf(".")==-1){//se non ha estensione
                    directoryWithoutSlash(outAlClient,dataOut,fileRequested+"/");
                }
                else{
                    fileNotFound(outAlClient, dataOut, fileRequested);
                }
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }
        }
        catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Errore nel recupero dei dati dal DB. "+ex.getLocalizedMessage());
        }
        finally {
            try {
                inDalClient.close();
                outAlClient.close();
                dataOut.close();
                //viene chiusa la connessione col client (connessione non persistente)
                clientSocket.close(); 
            } catch (IOException e) {
                System.err.println("Error closing stream : " + e.getMessage());
            } 
            if (VERBOSE) {
                System.out.println("Connection closed.\n");
            }
        }
    }
    /*
    
    */
    private Elenco retriveElenco() throws ClassNotFoundException, SQLException{
        ArrayList<Nominativo> nomi=new ArrayList<>();
        ResultSet res=new InterrogazioneDB().eseguiQuery("select nome, cognome from persone;");
        while (res.next()) {
            nomi.add(new Nominativo(res.getString(1), res.getString(2)));
        }
        return new Elenco(nomi);
    }
    /*
    
    */
    private File classToXML(Elenco el) throws IOException{
        XmlMapper xmlMapper = new XmlMapper();
        File fXML=new File(WEB_ROOT+"\\elenco.xml");
        if(!fXML.exists()){
            fXML.createNewFile();
        }
        xmlMapper.writeValue(fXML, el);
        return fXML;
    }
    /*
    
    */
    private File classToJSON(Elenco el) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        File fJSON=new File(WEB_ROOT+"\\elenco.json");
        if(!fJSON.exists()){
            fJSON.createNewFile();
        }
        objectMapper.writeValue(fJSON, el);
        return fJSON;
    }
    /*
    
    */
    private File fromJSONToXML() throws FileNotFoundException, IOException{
        //recupero il json
        File fJSON=new File(PATH+"\\src\\main\\java\\testgroup\\generalsources\\puntiVendita.json");
        //metti il contenuto del json su stringa
        String fileString=readFile(fJSON);
        //creo e configuro il json mapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        //creo la classe dal json
        ListaRisultati pv = objectMapper.readValue(fileString, ListaRisultati.class);
        //creo il mapper xml
        XmlMapper xmlMapper = new XmlMapper();
        //creo l'array di byte contenente l'xml della classe
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        xmlMapper.writeValue(byteArray, pv); 
        //trasformo l'array di byte in stringa
        String arrayXML=byteArray.toString();
        System.out.println(arrayXML);
        File fXML=new File(WEB_ROOT+"\\punti-vendita.xml");
        if(fXML.exists()){
            fXML.delete();//usando il write potrei evitarlo e crearlo solo se non exists
        }
        fXML.createNewFile();
        FileWriter fw=new FileWriter(fXML);
        fw.write(arrayXML);
        fw.close();
        return fXML;
    }
    /*
    
    */
    private String readFile(File f) throws FileNotFoundException, IOException{
        String ret="";
        FileReader fr=new FileReader(f);
        BufferedReader br=new BufferedReader(fr);
        for(;;){
            String appo=br.readLine();
            if(appo==null){
                break;
            }
            ret+=appo;
        }
        return ret;
    }
    /*
    
    */
    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        }
        finally {
            if (fileIn != null){
                fileIn.close();
            }
        }
        return fileData;
    }
    /*
    
    */
    //ritorna il tipi supportati
    private String getContentType(String fileRequested) {
        String ext=fileRequested.substring(fileRequested.lastIndexOf(".")+1);
        switch(ext){
            case "htm":
            case "html":
                return "text/html";
            case "png":
                return "image/png";
            case "xml":
                return "text/xml";
            case "json":
                return "application/json";
            default:
                return "text/plain";
        }
    }
    /*
    
    */
    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);
        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
        if (VERBOSE) {
            System.out.println("File " + fileRequested + " not found");
        }
    }
    /*
    
    */
    private void directoryWithoutSlash(PrintWriter out, OutputStream dataOut,String directoryRequested) throws IOException{
        File file = new File(WEB_ROOT, FILE_MOVED);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);
        out.println("HTTP/1.1 301 Moved Permanently");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        //probabile posizione della cartella
        out.println("Location: "+directoryRequested);
        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
        if (VERBOSE) {
            System.out.println("Directory " + directoryRequested + " hint sended");
        }
    }
}