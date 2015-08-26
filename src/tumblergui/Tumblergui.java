/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblergui;

/**
 *
 * @author milos
 */
public class Tumblergui {
    
    public void populateMachinesList() {
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String host = "";
        int port = 0;
        
        System.out.println("hello world");
        
        if (args.length != 2) {
            System.out.println("Insufficient/too many arguments provided. Provide host and port as arguments");
            System.out.println("Using defaults instead");
//            System.exit(0);              
            host = "localhost";
            port = 39998;
        } else {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        
        ApplicationWindow appwindow = new ApplicationWindow();
        
        new TumblerGuiController(appwindow).setHost(host).setPort(port).init().takeOver();
        
    } //main
}
