/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HVV_Communication;


import HVV_Communication.executors.AStatementExeRunnable;
import java.util.LinkedList;

/**
 *
 * @author yaroslav
 */
public class CommandItem {
    private final AStatementExeRunnable m_processor;
    public AStatementExeRunnable GetProcessor() { return m_processor;}
    
    private String m_strCommandId;
    /**
     * Get ID for current processing command transaction
     * @return
     *   id
     */
    public String GetCommandId() { return m_strCommandId; }
    public void SetCommandId( String strId) { m_strCommandId = strId; }
    
    private final LinkedList m_lstParcelOut;
    public LinkedList GetParcelOut() { return m_lstParcelOut; }
    
    private final LinkedList m_lstParcelIn;
    public LinkedList GetParcelIn() { return m_lstParcelIn; }
    
    public CommandItem( AStatementExeRunnable proc, LinkedList parcelOut) {
        m_processor = proc;                 //<-- Item processor is filled when creating this Item
        m_lstParcelOut = parcelOut;         //<-- ParcelOut is filled when creating this Item
        
        m_lstParcelIn = new LinkedList();   //<-- ParcelIn will be filled by TwoWaySocket.AddToQueue
        m_strCommandId = "";                //<-- Id will be filled by SerialWriter
    }
}
