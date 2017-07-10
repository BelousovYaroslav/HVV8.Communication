/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HVV_Communication.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 *
 * @author yaroslav
 */
public abstract class HVV_Comm_Server implements Runnable {
    static Logger logger = Logger.getLogger(HVV_Comm_Server.class);
    
    private int m_nState;
    public int GetState() { return m_nState; }
    public void SetState( int nNewState) { m_nState = nNewState; }
    
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED_OK = 1;
    public static final int STATE_CONNECTED_PROBLEMS = 2;
    public static final int STATE_CONNECTED_IDLE = 3;
    
    public Thread m_Thread;
    volatile boolean m_bContinue;
    private final int m_nServerPort;
    
    private Socket m_socket = null;
    public Socket GetSocket() { return m_socket; }
    
    private InputStream m_is = null;
    public InputStream GetInputStream() { return m_is; }
    
    private ObjectInputStream m_ois = null;
    public ObjectInputStream GetObjectInputStream() { return m_ois; }
    
    private ObjectOutputStream m_oos = null;
    public ObjectOutputStream GetObjectOutputStream() { return m_oos; }
    
    final String m_strMarker;
    
    protected int m_nStopRequested;
    
    public HVV_Comm_Server( String strMarker, int nServerPort) {
        m_strMarker = strMarker;
        m_Thread = null;
        m_nState = STATE_DISCONNECTED;
        m_nServerPort = nServerPort;
    }
    
    public void start() {
        if( m_Thread != null && m_Thread.isAlive() == true)
            return;
        
        m_nStopRequested = 0;
        m_Thread = new Thread( this);
        m_Thread.start();
    }
    
    public void stop() {
        logger.debug( m_strMarker + "STOP requested!");
        if( m_Thread != null) {
            try {
                if( m_nState != STATE_DISCONNECTED)
                    m_nStopRequested = 1;
                else
                    m_bContinue = false;
                
                m_Thread.join();
                m_nState = STATE_DISCONNECTED;
                m_Thread = null;
                    
            } catch( InterruptedException ex) {
                logger.warn( m_strMarker + "InterruptedException caught!", ex);
            }
        }
        logger.debug( m_strMarker + "Thread stopped!");
    }

    public abstract void processIncomingCommand( String strReqId, LinkedList lstIncomingParcel) throws Exception;
            
    @Override
    public void run() {
        m_bContinue = true;
        
        ServerSocket serverSocket = null;
        
        
        int nIdleCounter = 0;
        
        do {
            if( m_nState == STATE_CONNECTED_OK || m_nState == STATE_CONNECTED_IDLE) {
                try {
                    if( m_is != null && m_is.available() > 0) {
                        
                        nIdleCounter = 0;
                        m_nState = STATE_CONNECTED_OK;
                        
                        String strReqId = ( String) m_ois.readObject();
                        String strLog = "<< [" + strReqId + ";";
                        
                        int nParcelLen = m_ois.readInt();
                        strLog += "" + nParcelLen + ";";
                        
                        LinkedList lstIncomingParcel = new LinkedList();
                        for( int i=0; i< nParcelLen; i++) {
                            Object obj = m_ois.readObject();
                            lstIncomingParcel.addLast( obj);
                            strLog += obj + ";";
                        }
                        
                        strLog += "]";
                        logger.debug( m_strMarker + strLog);
                        
                        processIncomingCommand( strReqId, lstIncomingParcel);
                    }
                    else {
                        //NO INCOMING DATA
                        sleep( 100);
                        nIdleCounter++;
                        if( ( nIdleCounter % 10) == 0) {
                            int nSecondsOfIdle = nIdleCounter / 10;
                            if( nSecondsOfIdle > 5) {
                                logger.warn( m_strMarker + nSecondsOfIdle + " seconds of Idle...");
                                
                                if( nSecondsOfIdle >= 10) {
                                    m_nState = STATE_CONNECTED_IDLE;
                                
                                    if( nSecondsOfIdle >= 15) {
                                        logger.warn( m_strMarker + "Disconnecting!");
                                        m_nState = STATE_DISCONNECTED;
                                    }
                                }
                            }
                        }
                    }
                }
                catch( Exception ex) {
                    logger.error( m_strMarker + "IOException caught", ex);

                    if( m_ois != null) {
                        try { m_ois.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии входящего потока", ex2);}
                        m_ois = null;
                    }
                    if( m_oos != null) {
                        try { m_oos.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии исходящего потока", ex2);}
                        m_oos = null;
                    }
                    if( m_socket!= null) {
                        if( !m_socket.isClosed())
                            try { m_socket.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии сокета", ex2);}
                        m_socket = null;
                    }
                    if( serverSocket != null) {
                        if( !serverSocket.isClosed())
                            try { serverSocket.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии серверного сокета", ex2);}
                        serverSocket = null;
                    }
                    
                    m_nState = STATE_DISCONNECTED;
                }
            }
            else {
                //Clean up sockets and streams
                m_nState = STATE_DISCONNECTED;
                if( m_ois != null) {
                    try { m_ois.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии входящего потока", ex2);}
                    m_ois = null;
                }
                if( m_oos != null) {
                    try { m_oos.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии исходящего потока", ex2);}
                    m_oos = null;
                }
                if( m_socket!= null) {
                    if( !m_socket.isClosed())
                        try { m_socket.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии сокета", ex2);}
                    m_socket = null;
                }
                if( serverSocket != null) {
                    if( !serverSocket.isClosed())
                        try { serverSocket.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при аварийном закрытии серверного сокета", ex2);}
                    serverSocket = null;
                }
                
                if( m_nStopRequested == 2) {
                    logger.info( "Quit requested, quit received. Quitting.");
                    break;
                }
                
                try {
                    serverSocket = new ServerSocket( m_nServerPort);
                    serverSocket.setSoTimeout( 5000);
        
                    logger.info( m_strMarker + "Waiting for a connection on " + m_nServerPort);
                
                    m_socket = serverSocket.accept();
                    
                    logger.info( m_strMarker + "Connection accepted! Creating streams");
                    m_is = m_socket.getInputStream();
                    m_oos = new ObjectOutputStream( m_socket.getOutputStream());
                    m_ois = new ObjectInputStream( m_socket.getInputStream());
            
                    m_socket.setKeepAlive( true);
                    m_socket.setSoLinger( true, 5);
                    
                    nIdleCounter = 0;
                    m_nState = STATE_CONNECTED_OK;
                }
                catch( SocketTimeoutException ex) {
                    logger.warn( m_strMarker + "Server connection timeout! Restarting!");
                    m_nState = STATE_DISCONNECTED;
                }
                catch( Exception ex) {
                    logger.warn( m_strMarker + "Exception caught while waiting for incoming connecton!", ex);
                    m_nState = STATE_DISCONNECTED;
                }
            }
            logger.trace( m_strMarker + "m_bContinue:" + m_bContinue);
        } while( m_bContinue);
        
        logger.info( m_strMarker + "Closing streams");
        if( m_ois != null) {
            try { m_ois.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при финальном закрытии входящего потока", ex2);}
            m_ois = null;
        }
        if( m_oos != null) {
            try { m_oos.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при финальном закрытии исходящего потока", ex2);}
            m_oos = null;
        }
        if( m_socket!= null) {
            if( !m_socket.isClosed())
                try { m_socket.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при финальном закрытии сокета", ex2);}
            m_socket = null;
        }
        
        logger.info( m_strMarker + "Closing server socket...");
        if( serverSocket != null) {
            if( !serverSocket.isClosed())
                try { serverSocket.close(); } catch( IOException ex2) { logger.error( m_strMarker + "Exception при финальном закрытии серверного сокета", ex2);}
        }
    }
}
