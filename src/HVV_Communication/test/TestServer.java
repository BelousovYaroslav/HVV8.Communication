/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HVV_Communication.test;

import HVV_Communication.server.HVV_Comm_Server;
import java.util.LinkedList;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 *
 * @author yaroslav
 */
public class TestServer {
    static HVV_Communication.server.HVV_Comm_Server m_srv;
    
    static Logger logger = Logger.getLogger( TestServer.class);
    
    public static void main( String[] args) {
        BasicConfigurator.configure();
        logger.setLevel( org.apache.log4j.Level.TRACE);
        
        try {
            m_srv = new HVV_Comm_Server( "TST_SRV: ", 1234) {

                @Override
                public void processIncomingCommand( String strReqId, LinkedList lstIncomingParcel) throws Exception {
                    String strCmd = "";

                    int nRetCode = 0;

                    strCmd = ( String) lstIncomingParcel.get( 0);
                    if( strCmd != null) {
                        switch( strCmd) {

                            case "PING":
                                //logger.debug( "INCOMING: [" + strReqId + ";PING;" + "]");
                                nRetCode = 0;
                            break;

                            case "QUIT":
                                //logger.info( "'QUIT' processing");
                                SetState( STATE_DISCONNECTED);
                                return;

                            default:
                                logger.error( "" + strReqId + ": Unknown command '" + strCmd + "'. RetCode 3");
                                nRetCode = 3;
                            break;
                        }
                    }
                    else {
                        logger.error( "" + strReqId + ": Command is null. RetCode 2");
                        nRetCode = 2;
                    }


                    //RESPOND
                    logger.debug( ">> [" + strReqId + ";" + nRetCode + "]");

                    GetObjectOutputStream().writeObject( strReqId);
                    GetObjectOutputStream().writeInt( 1);
                    
                    if( m_nStopRequested == 1) {
                        GetObjectOutputStream().writeObject( 100);
                        m_nStopRequested = 2;
                    }    
                    else 
                        GetObjectOutputStream().writeObject( nRetCode);
                    
                    GetObjectOutputStream().flush();
                }
            };
            
            m_srv.start();
            m_srv.m_Thread.join( 20000);
            
            logger.debug( "Now, we want to exit");
            m_srv.stop();
            
        
        } catch ( Exception ex) {
            logger.error( "Exception caught!", ex);
        }
        
    }
}
