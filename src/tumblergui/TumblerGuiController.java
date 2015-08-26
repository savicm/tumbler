/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tumblergui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import tumblerdb.DB;

/**
 *
 * @author milos
 */
public class TumblerGuiController {

    private ApplicationWindow appwindow;
    private String host;
    private int port;
    
    
    
    public TumblerGuiController(ApplicationWindow appwindow) {
        this.appwindow = appwindow;
        final JCheckBox all = this.appwindow.getAllCheckbox();
        all.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                System.out.println("check status change: " + all.isSelected());
                TumblerGuiController.this.drawMachineTable();
            }
        });
    }

    public TumblerGuiController setHost(String host) { this.host = host; return this;}
    public TumblerGuiController setPort(int port) { this.port = port; return this;}
    
    public TumblerGuiController init() {
        JPanel jp1 = appwindow.getjPanel1();
        jp1.setLayout(new GridLayout(20,1));
        JPanel jp2 = appwindow.getjPanel2();
        jp2.setLayout(new GridLayout(2,1));
        
        //set attribute search field listener
        JTextField searchAttrField = this.appwindow.getSearchAttrField();
        searchAttrField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                TumblerGuiController.this.populateAttributes(TumblerGuiController.this.appwindow.getSearchAttrField().getText());
            }
            public void removeUpdate(DocumentEvent e) {
                TumblerGuiController.this.populateAttributes(TumblerGuiController.this.appwindow.getSearchAttrField().getText());
            }
            public void insertUpdate(DocumentEvent e) {
                TumblerGuiController.this.populateAttributes(TumblerGuiController.this.appwindow.getSearchAttrField().getText());
            }
        });
        
        //set machines search field listener
        JTextField searchMachinesField = this.appwindow.getSearchMachinesField();
        searchMachinesField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                TumblerGuiController.this.populateMachines(TumblerGuiController.this.appwindow.getSearchMachinesField().getText());
            }
            public void removeUpdate(DocumentEvent e) {
                TumblerGuiController.this.populateMachines(TumblerGuiController.this.appwindow.getSearchMachinesField().getText());
            }
            public void insertUpdate(DocumentEvent e) {
                TumblerGuiController.this.populateMachines(TumblerGuiController.this.appwindow.getSearchMachinesField().getText());
            }
        });
        
        
        appwindow.setTitle("Tumbler");
        appwindow.setVisible(true);
        appwindow.addComponentListener(new ComponentListener() {
          public void componentResized(ComponentEvent e) {
              appwindow.revalidate();
              appwindow.repaint();
              appwindow.getjPanel2().revalidate();
          }  
          
          public void componentHidden(ComponentEvent e) {
              // do nothing, i guess
          }
          
          public void componentShown(ComponentEvent e) {
              appwindow.revalidate();
              appwindow.repaint();
          }
           
          public void componentMoved(ComponentEvent e) {
              appwindow.revalidate();
              appwindow.repaint();
          }
        });
        return this;
    }
    
    public void takeOver() {
        //handle stuff
        this.populateMachines();
//        this.populateAttributes();
    }
    
    public void populateAttributes(String search) {
//        String machinekey = "";
        JPanel jp1 = this.appwindow.getjPanel1();
        Component[] components = jp1.getComponents();
        JCheckBox all = this.appwindow.getAllCheckbox();
        String machinekey = "";
        if(all.isSelected()) {
            machinekey = "%"; 
        }
        else { 
            JList list = (JList)jp1.getComponent(0);
            System.out.println("Populate attributes, selected value: "+list.getSelectedValue().toString());
            machinekey = list.getSelectedValue().toString();
        }
        this.populateAttributes(machinekey, search);
    }
    
    public void populateAttributes(String machinekey, String search) {
        this.drawMachineTable(machinekey, search);
    }

    public void populateAttributes() {
        this.populateAttributes("");
    }


    
    public void populateMachines(String search) {
        HashMap<String, Object> machines = null;
        
        TumblerCommand command = new TumblerCommand(host, port);
        machines = Parser.parseJSON(command.sendCommand("searchmach "+search));
        
        JPanel jp1 = this.appwindow.getjPanel1();
        jp1.removeAll();
        final JPanel jp2 = this.appwindow.getjPanel2();
        
        DefaultListModel listmodel = new DefaultListModel();

        Iterator i = machines.entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>)i.next();
            listmodel.addElement(entry.getValue());
        }
        final JList list = new JList(listmodel);
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if(!event.getValueIsAdjusting()) {
                    TumblerGuiController.this.populateAttributes(list.getSelectedValue().toString(), "");
                 //   jp2.revalidate();
                }
            }
        });
            jp1.add(list);
        jp1.revalidate(); //recalculate layout. required
    }
    
    public void populateMachines() {
        this.populateMachines("");
    }

    public void drawMachineTable() {
        System.out.println("drawMachineTable called");
        String search = this.appwindow.getSearchAttrField().getText().toString();
        String machinekey = "";
        JCheckBox all = this.appwindow.getAllCheckbox();
        if(all.isSelected()) {
            machinekey = "%"; 
        } else {
            JPanel jp1 = this.appwindow.getjPanel1();
            JList list = (JList)jp1.getComponent(0);
            machinekey = list.getSelectedValue().toString();
        }
        this.drawMachineTable(machinekey, search);
    }
    
    public void drawMachineTable(String machinekey) {
        drawMachineTable(machinekey, "");
    }
    
    public void drawMachineTable(String machinekey, String searchattr) {
        JPanel jp2 = this.appwindow.getjPanel2();
        jp2.removeAll();
        TumblerCommand tumblerCommand = new TumblerCommand(host, port);
        System.out.println("drawMachineTable, machinekey: "+machinekey+", searchattr: "+searchattr);
        String response = (String)tumblerCommand.sendCommand("searchattr " + machinekey + (searchattr.equals("")?"":" " + searchattr));
        HashMap<String, Object> machine = Parser.parseJSON(response);
//        System.out.println("drawmachintable json response: "+response);
        Iterator iter = machine.entrySet().iterator();
        String[][] rows = new String[machine.size()][];
        int i = 0;
        while(iter.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>)iter.next();
            rows[i++] = new String[]{(String)entry.getKey(), (String)entry.getValue()};
        }
        
        JTable table = new JTable(rows, new String[]{"parameter", "data"});
        JScrollPane jscrollpane = new JScrollPane(table);
        jp2.add(jscrollpane);
        table.setVisible(true);
        jp2.revalidate();
    }    
}