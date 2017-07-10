/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HVV_Communication.executors;

import HVV_Communication.CommandItem;
import HVV_Communication.client.HVV_Comm_client;
import static java.lang.Thread.sleep;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 *
 * @author yaroslav
 */
public class PingRunnable extends AStatementExeRunnable {

    static Logger logger = Logger.getLogger(PingRunnable.class);            
    
    private boolean m_bGotAnswer;
    public boolean GetPingOk() { return m_bGotAnswer; }
    
    private boolean m_bTimeOut;
    public boolean GetPingTimeOut() { return m_bTimeOut; }

    private boolean m_bContinue;
    public void StopThread() {
        m_bContinue = false;
    }
    
    HVV_Comm_client m_pCommThread;
    public PingRunnable( HVV_Comm_client pCommThread) {
        super();
        m_pCommThread = pCommThread;
    }

    @Override
    public void run() {
        
        
        m_bContinue = true;
        boolean bProcessing = false;
        
        m_bGotAnswer = false;
        m_bTimeOut = false;

        long lLastQueuedPing = 0;
        
        do {
            
            if( bProcessing == true) {
                if( m_bGotAnswer == true) {
                    logger.debug( m_pCommThread.m_strMarker + "PING; RESPONDED;");
                    bProcessing = false;
                    
                }
                
                if( m_bTimeOut == true) {
                    logger.warn( m_pCommThread.m_strMarker + "PING; TIMEOUT;");
                    bProcessing = false;
                }
                
            }
            else {
                m_bGotAnswer = false;
                m_bTimeOut = false;
        
                if( m_pCommThread.GetRxTx().GetQueue().isEmpty()) {
                    
                    if( lLastQueuedPing < System.currentTimeMillis()) {
                        
                        lLastQueuedPing = System.currentTimeMillis() + 5000;
                        
                        LinkedList lst = new LinkedList();
        
                        //PING
                        lst.addLast( "PING");

                        //ADDING COMMAND TO OUTPUT QUEUE WITH MENTION ABOUT ITSELF AS PROCESSOR
                        CommandItem item = new CommandItem( this, lst);
                        m_pCommThread.GetRxTx().AddCommandToQueue( item);
                
                        logger.info( m_pCommThread.m_strMarker + "PING; QUEUED;");
                        bProcessing = true;
                    }                    
                }
            }

            try {
                sleep( 100);
            } catch (InterruptedException ex) {
                logger.error( m_pCommThread.m_strMarker + "InterruptedException caught!", ex);
            }
            
        } while( m_bContinue);
    }
    
    @Override
    public void processResponse( LinkedList lstResponseParcel) {
        //VERIFY length == 1
        int nCode = ( int) lstResponseParcel.get( 0);
        logger.debug( m_pCommThread.m_strMarker + "processResponse( " + nCode + ") call for PingThread.");
        
        if( nCode == 100) {
            logger.info( "Server want to exit! Disconnecting!");
            
            try {
                /*
                m_pCommThread.m_bStopRequested = true;
                */
                LinkedList lstQuitCmd = new LinkedList();
                lstQuitCmd.addLast( "QUIT");
                CommandItem quitItem = new CommandItem( null, lstQuitCmd);
                m_pCommThread.GetRxTx().AddCommandToQueue( quitItem);
                
                m_pCommThread.m_bServerStopRequested = true;
            } catch( Exception ex) {
                logger.error( "Exception caught!", ex);
            }
        }
        m_bGotAnswer = true;
    }

    @Override
    public void processTimeOut() {
        logger.warn( m_pCommThread.m_strMarker + "processTimeOut() call for PingThread. Empty statement!");
        m_bTimeOut = true;
    }
    
    
}
