/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblerserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import tumblerdb.DB;

/**
 *
 * @author milos
 */
public class TumblerServerCommandThread extends Thread {

    public TumblerServerCommandThread() {
                
    }
    
    public void process(SSLSocket clientSocket) throws IOException {

        BufferedReader in = null;
        PrintWriter out = null;    
        DB db = DB.getInstance();
        System.out.println("Command worker thread spawned. Processing request...");

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(TumblerServerCommandThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        String inputline, outputline;

        // expect one line
//       while((inputline = in.readLine()) != null) {
//        System.out.println("CommandThread: Received command: " + inputline);
//       }
         
        inputline = in.readLine();
        System.out.println("CommandThread: Received command: " + inputline);
        String result = "";
        
        if(inputline.contains("searchmach")) {
                    String searchfor = inputline.replace("searchmach ", "");
                    System.out.println("Processing search machines request");
                    result = db.getSearchMachines(searchfor);
                    out.println(result);
                    System.out.println("Done processing search machines request");
        }
        else if(inputline.contains("searchattr")) {
                    String[] parameters = inputline.split(" ");
                    String instanceid = parameters[1];
                    String searchattr = "";
                    if(parameters.length == 3) searchattr = parameters[2];
                    
                    System.out.println("Processing search atributes request");
                    result = db.getSearchAttributes(instanceid, searchattr);
                    out.println(result);
                    System.out.println("Done processing search attributes request");
        }
        else if(inputline.contains("instance")) {
                    String machine = inputline.replace("instance ", "");
                    System.out.println("Processing single machine request");
                    result = db.getMachine(machine);
                    out.println(result);
                    System.out.println("Done processing single machine request");
        }
        else { // a plain single command
            switch(inputline) {
                case "all_machines":
                    System.out.println("Processing all_machines request");
                    result = db.getAllMachines();
                    out.println(result);
                    System.out.println("Done processing all_machines request");
                break;
                default:
                    System.out.println("Unrecognized command");
                    break;
            }
        }
       
        out.close();
        in.close();
    }
    
}
