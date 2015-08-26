/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblergui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
//import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author milos
 */
public class TumblerCommand {
    
    private String host;
    private int port;
            
    public TumblerCommand(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public String sendCommand(String command){
        String result = "";
        try {
            System.out.println("Sending command to server: " + command);
            
            System.setProperty("javax.net.ssl.trustStore","/home/milos/NetBeansProjects/Tumbler/tumblerKeystore");
            System.setProperty("javax.net.ssl.trustStorePassword","changeit");

            SSLSocketFactory sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
//            Socket clientSocket = new Socket(host, port);
            SSLSocket clientSocket = (SSLSocket)sslSocketFactory.createSocket(host, port);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            out.println(command);
            
            String inputline = "";
            
            while((inputline = in.readLine()) != null) {            
                System.out.println("Received line: " + inputline);
                result += inputline;
            }
            
        } catch(IOException e) {
            System.out.println("Error sending a command to server");
        }
        
        return result;
    }
    
}
