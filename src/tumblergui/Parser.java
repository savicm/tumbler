/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblergui;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author milos
 */
public class Parser {

    public static String parseFacter(String facterinfo) {
        String newline = "\n";
        String str_delimiters = "=>";
        HashMap<String, String> mapInfo = new HashMap<String, String>();
        String[] lines = facterinfo.split(newline);
        String[] templine = null;
        String jsoninfo ="";
        for(String line:lines) {
            templine = line.split(str_delimiters);
            if(templine == null || templine.length<2 || templine[0] == null) continue;
            mapInfo.put(templine[0].trim(), templine[1].trim());
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsoninfo = objectMapper.writeValueAsString(mapInfo);
        } catch (JsonProcessingException ex) {
            System.out.println("Error processing json from info");
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return jsoninfo;
    }    

    public static String encodeJSON(Object info) {
        ObjectMapper objectMapper = new ObjectMapper();
        String result = "";
        try {
            result = objectMapper.writeValueAsString(info);
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public static HashMap<String, Object> parseJSON(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, Object> map = null;
        try {
            map = objectMapper.readValue(json, HashMap.class);
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }
}
