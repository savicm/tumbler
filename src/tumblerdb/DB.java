/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblerdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import tumblergui.Parser;
import tumblerserver.TumblerServer;

/**
 *
 * @author milos
 */
public class DB {
    
    private String dbdriver = "org.apache.derby.jdbc.EmbeddedDriver";
    private Connection conn = null;
    private String sql_init = "create table data(id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
            + "instanceid VARCHAR(30), timestamp TIMESTAMP, parameter VARCHAR(100), data VARCHAR(1000), CONSTRAINT primary_key PRIMARY KEY(id))";
    private String sqlDataInsert = "insert into data(instanceid, timestamp, parameter, data) values(?, ?, ?, ?)";

    private PreparedStatement table_init = null;
    private PreparedStatement stmtDataInsert = null;
    
    private static DB instance = null;
    
    private DB() {
        try {
            this.conn = DriverManager.getConnection("jdbc:derby:tumbler;create=true");
            System.out.println("Established db connection...");
        } catch (SQLException ex) {
            Logger.getLogger(TumblerServer.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }

    public static DB getInstance() {
        if(instance == null) instance = new DB();
        return instance;
    }
    
    public void prepareDB() {
        //prepare the table
        try {
             this.table_init = this.conn.prepareStatement(sql_init);
             this.table_init.execute();
        } catch (SQLException e) {
//            Logger.getLogger(TumblerServer.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Exception in prepareDB: Table \"data\" might already exist.");
//            e.printStackTrace();
        }
    }
    
    public void insertRow(String instanceid, String timestamp, String property, String value) throws SQLException {
        ResultSet rs = null;
        stmtDataInsert = this.conn.prepareStatement(sqlDataInsert);
        stmtDataInsert.setString(1, instanceid);
        stmtDataInsert.setString(2, timestamp);
        stmtDataInsert.setString(3, property);
        stmtDataInsert.setString(4, value);
        stmtDataInsert.execute();
    }
    
    public HashMap selectQuery(String sql) throws SQLException{
        HashMap<Integer, String[]> result = new HashMap<Integer, String[]>();
        Statement stmt = this.conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()) {
            if(rs == null) break;          
            String[] rez = {
                rs.getString("instanceid"), 
                (rs.getTimestamp("timestamp")!=null?rs.getTimestamp("timestamp").toString():""), 
                rs.getString("parameter"), 
                rs.getString("data")
            };
            int id = rs.getInt("id");
            result.put(id, rez);
        }
        return result;
    }
    
    public String getAllMachines(){
        String sql = "select distinct instanceid from data";
        String jsonResult = "";        
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            HashMap<String, String> result = new HashMap<String, String>();
            int count = 0;
            while(rs.next()) {
                result.put("instanceid_" + count++, rs.getString("instanceid"));
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                jsonResult = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
                return "{\"Error processing result\"}";
            }
        } catch (SQLException e) {
            return "{\"Error in SQL processing\"} "+e.getMessage();
        }
        
        return jsonResult;
    }

    public String getSearchMachines(String search){
        String sql = "select distinct instanceid from data where instanceid like ?";
        String jsonResult = "";        
        try {
            PreparedStatement stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();
            HashMap<String, String> result = new HashMap<String, String>();
            int count = 0;
            while(rs.next()) {
                result.put("instanceid_" + count++, rs.getString("instanceid"));
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                jsonResult = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
                return "{\"Error processing result\"}";
            }
        } catch (SQLException e) {
            return "{\"Error in SQL processing\"} "+e.getMessage();
        }
        
        return jsonResult;
    }

        public String getSearchAttributes(String instanceid, String search){
        String sql = "select instanceid, parameter, data from data where (parameter like ? or data like ?)";
        if(!instanceid.equals("%")) sql+=" and instanceid=?";
        sql += " order by timestamp asc, instanceid, parameter, data"; //timestamp asc, da bi poslednji set podataka bio najnoviji, i takav se upisao (stari podaci se gaze)
        System.out.println("getSeachAttributes, query: "+sql);
        String jsonResult = "";        
        try {
            System.out.println("getSearchAttributes, instanceid: "+instanceid+", search: "+search);
            PreparedStatement stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, "%" + search + "%");
            stmt.setString(2, "%" + search + "%");
            if(!instanceid.equals("%")) stmt.setString(3, instanceid);
            ResultSet rs = stmt.executeQuery();
            HashMap<String, String> result = new HashMap<String, String>();
            int count = 0;
            while(rs.next()) {
                result.put(rs.getString("instanceid")+": "+rs.getString("parameter"), rs.getString("data"));
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                jsonResult = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
                return "{\"Error processing result\"}";
            }
        } catch (SQLException e) {
            return "{\"Error in SQL processing\"}"+e.getMessage();
        }
        
        return jsonResult;
    }
        
    public String getMachine(String machineid) {
        String sql = "select * from data where instanceid = ?";
        String jsonResult = "";
        HashMap<String, String> tempResult = new HashMap<String, String>();
        try {
            PreparedStatement stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, machineid);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                tempResult.put(rs.getString("parameter"), rs.getString("data"));
            }
            jsonResult = Parser.encodeJSON(tempResult);
        } catch(SQLException e) {
            System.out.println("Error in sql while getting a machine");
            e.printStackTrace();
        }
        
        return jsonResult;
    }
}
