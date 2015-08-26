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
import java.net.Socket;
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
public class TumblerServerDataThread extends Thread {

    public TumblerServerDataThread() {
        
        
    }
    
    public void process(Socket clientSocket) throws IOException {

        BufferedReader in = null;
        PrintWriter out = null;    
        DB db = DB.getInstance();
        System.out.println("Worker thread spawned. Processing request...");

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException ex) {
                Logger.getLogger(TumblerServerDataThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        
            String inputline, outputline;
            String info = new String();
            String sql = "";
            String instanceid = "";
            String timestamp = "";

            while((inputline = in.readLine()) != null) {            
                if(inputline.equals("!!end")) break;
                System.out.println(inputline);
                info += inputline;
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> mapInfo = null;        
            try {
               mapInfo = mapper.readValue(info, Map.class);
            } catch (IOException ex) {
               Logger.getLogger(TumblerServerDataThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            for(Map.Entry<String, String> entry:mapInfo.entrySet()) {
                System.out.println(entry.getKey() + " ! " + entry.getValue());
            }

            //insert the data 
            try{
                instanceid = mapInfo.get("instance-id");
                if(instanceid == null) instanceid = mapInfo.get("ec2_instance_id");
                if(instanceid == null) instanceid = mapInfo.get("hostname");
                timestamp = mapInfo.get("timestamp");
                if(timestamp == null) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();
                    timestamp = dateFormat.format(date);
                }
                
                for(Map.Entry<String, String> entry:mapInfo.entrySet()) {
                    db.insertRow(instanceid, timestamp, entry.getKey(), entry.getValue());
                    System.out.println(instanceid + " ! " + timestamp + " ! " + entry.getKey() + " ! " + entry.getValue());
                }
            } catch(SQLException e) {
                System.out.println("Insert data error");
                e.printStackTrace();
            }
            
            out.close();
            in.close();
    }
    
}
