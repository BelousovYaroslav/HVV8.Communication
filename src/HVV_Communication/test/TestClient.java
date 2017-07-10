/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HVV_Communication.test;

import HVV_Communication.client.HVV_Comm_client;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 *
 * @author yaroslav
 */
public class TestClient {
    static Logger logger = Logger.getLogger( TestClient.class);
    
    static HVV_Comm_client m_cli;
    
    public static void main( String[] args) {
        BasicConfigurator.configure();
        logger.setLevel( org.apache.log4j.Level.TRACE);
        
        try {
            m_cli = new HVV_Comm_client( "TEST_CLI: ", "localhost", 1234);
            
            m_cli.start();
            m_cli.m_Thread.join( 40000);
            
            m_cli.stop( true);
            
        } catch( Exception ex) {
            logger.error( "Exception caught!", ex);
        }
    }
}
