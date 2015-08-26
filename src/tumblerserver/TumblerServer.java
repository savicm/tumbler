/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblerserver;

import java.io.*;

import tumblerdb.DB;

/**
 *
 * @author milos
 */
public class TumblerServer {
    
    public static void main(String[] args) throws IOException {
        
            DB db = DB.getInstance();
            db.prepareDB();
        
            new TumblerServerListenerThread(39999, "data").start();
            new TumblerServerListenerThread(39998, "command").start();

    } //main   
        
} 
