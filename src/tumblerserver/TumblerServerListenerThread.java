/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblerserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
//import java.net.ServerSocket;
//import java.net.Socket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 *
 * @author milos
 */
public class TumblerServerListenerThread extends Thread {
    
    private int port;
    private String threadType;
    public TumblerServerListenerThread(int port, String threadType) {
        this.port = port;
        this.threadType = threadType;
    }
    
    @Override
    public void run() {
//        ServerSocket serverSocket = null;
        SSLServerSocketFactory sslserversocketfactory;
        SSLServerSocket serverSocket = null;
                
        try {
            
//            System.setProperty("javax.net.ssl.trustStore","/home/milos/NetBeansProjects/Tumbler/tumblerKeystore");
//            System.setProperty("javax.net.ssl.trustStorePassword","changeit");

            SSLContext ctx = SSLContext.getInstance("SSL");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream("/home/milos/NetBeansProjects/Tumbler/tumblerKeystore"), "changeit".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "changeit".toCharArray()); // That's the key's password, if different.
            // ...
            ctx.init(kmf.getKeyManagers(), null, null);
            
            sslserversocketfactory = (SSLServerSocketFactory)ctx.getServerSocketFactory();
            serverSocket = (SSLServerSocket)sslserversocketfactory.createServerSocket(port);
            
          System.out.println("Listener(" + threadType + ") bound to port " + port + "\n");
        } catch(IOException e) {
            System.out.println("Couldn't bind to port " + port + "\n");
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TumblerServerListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(TumblerServerListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(TumblerServerListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(TumblerServerListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(TumblerServerListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SSLSocket clientSocket = null;
        
        while(true) {
            try {
                clientSocket = (SSLSocket)serverSocket.accept();
                switch(threadType) {
                    case "data": 
                        System.out.println("Accepting data..");
                        new TumblerServerDataThread().process(clientSocket);
                        break;
                    case "command":
                        System.out.println("Accepting a command..");
                        new TumblerServerCommandThread().process(clientSocket);
                        break;
                }
            } catch (IOException e) {
                System.out.println("Couldn't accept");
                e.printStackTrace();
                break;
            }
        }

        try {
            clientSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println("Couldn't close sockets");
            Logger.getLogger(TumblerServerListenerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.exit(1);
        
        
    }
    
}
