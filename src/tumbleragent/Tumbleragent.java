/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumbleragent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.Socket;
import javax.net.ssl.SSLSocket;
//import javax.net.ssl.SSLServerSocketFactory;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;
import tumblergui.Parser;

/**
 *
 * @author milos
 */
public class Tumbleragent {
    
    public Tumbleragent() {
    }

    public static void main(String[] args) {
        try {
            String host ="";
            int port = 0;
            if(args.length < 1) {
                host= "localhost";
                port = 39999;
            } else {
                System.out.println(args[0]);
                host = args[1];
                port = Integer.parseInt(args[2]);
            }
            Tumbleragent.collectData(host, port);
        } catch (IOException e) {
            System.out.println("Error while collecting data. Possibly facter fault or error communicating with server");
            e.printStackTrace();
            System.exit(1);
        }
    }
    

    public static void collectData(String host, int port) throws IOException {

        int exitValue = -1;
        String output = "", line = "";
        
        // execute facter, catch output, fail if no facter installed too
        try {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec("facter");
            InputStream stdout = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdout);
            BufferedReader br = new BufferedReader(isr);

            while((line = br.readLine()) != null) output += line.trim() + "\n";
            
            exitValue = p.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(Tumbleragent.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("facter execution interrupted..");
            System.exit(1);
        }
        
        
        System.out.println("facter has returned a " + exitValue + " return value");
        
        output += "!!end";
        
        String info = Parser.parseFacter(output);
        
        if(host.equals("") || host == null) host = "localhost";
        if(port == 0) port = 39999;
        
        System.setProperty("javax.net.ssl.trustStore","/home/milos/NetBeansProjects/Tumbler/tumblerKeystore");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");

        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket)sslSocketFactory.createSocket("localhost", 39999);

//        Socket socket = new Socket("localhost", 39999);
        
        PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
        out.println(info);
        out.close();
    }
    
}
