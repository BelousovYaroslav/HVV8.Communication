/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HVV_Communication.test;

import HVV_Communication.server.HVV_Comm_Server;
import static java.lang.Thread.sleep;
import java.util.LinkedList;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 *
 * @author yaroslav
 */
public class TestServerDlg extends javax.swing.JFrame {
    static Logger logger = Logger.getLogger( TestServerDlg.class);
    
    class StateRefresh implements Runnable {
        private boolean m_bContinue;
        
        public void stop() {
            m_bContinue = false;
        }
        
        @Override
        public void run() {
            m_bContinue = true;
            
            do {
                try {
                    if( m_srv != null) {
                        switch( m_srv.GetState()) {
                            case HVV_Comm_Server.STATE_DISCONNECTED:
                                lblClientState.setText( "DISCONNECTED!");
                            break;
                            case HVV_Comm_Server.STATE_CONNECTED_OK:
                                lblClientState.setText( "CONNECTED.OK!");
                            break;
                            case HVV_Comm_Server.STATE_CONNECTED_IDLE:
                                lblClientState.setText( "CONNECTED.IDLE!");
                            break;
                            case HVV_Comm_Server.STATE_CONNECTED_PROBLEMS:
                                lblClientState.setText( "CONNECTED.PROBLEMS!");
                            break;
                            default:
                                lblClientState.setText( "UNKNOWN!");
                            break;
                        }
                    }
                    else {
                        lblClientState.setText( "m_cli is NULL!");
                    }
                    sleep( 1000);
                } catch( InterruptedException ex) {
                    logger.error( "InterruptedException caught!", ex);
                }
            } while( m_bContinue);
        }
        
    }
            
    StateRefresh m_Refresher;
    Thread m_RefresherThread;
    
    HVV_Comm_Server m_srv;
    
    /**
     * Creates new form TestClientDlg
     */
    public TestServerDlg() {
        initComponents();
        setTitle( "СЕРВЕР");

        m_Refresher = new StateRefresh();
        m_RefresherThread = new Thread( m_Refresher);
        m_RefresherThread.start();
        
        m_srv = new HVV_Comm_Server("", 1234) {

            @Override
            public void processIncomingCommand(String strReqId, LinkedList lstIncomingParcel) throws Exception {
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
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblClientState = new javax.swing.JLabel();
        btnExit = new javax.swing.JButton();
        lblClientStateTitle = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(400, 130));
        setResizable(false);
        getContentPane().setLayout(null);

        lblClientState.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblClientState.setText("-");
        getContentPane().add(lblClientState);
        lblClientState.setBounds(160, 10, 220, 30);

        btnExit.setText("Выход");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        getContentPane().add(btnExit);
        btnExit.setBounds(10, 50, 380, 40);

        lblClientStateTitle.setText("Состояние связи:");
        getContentPane().add(lblClientStateTitle);
        lblClientStateTitle.setBounds(10, 10, 150, 30);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        
        try {
            m_srv.stop();
            
            m_Refresher.stop();
            m_RefresherThread.join();
            
        } catch( InterruptedException ex) {
            logger.error( "InterruptedException caught!", ex);
        }
        dispose();
    }//GEN-LAST:event_btnExitActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        BasicConfigurator.configure();
        logger.setLevel( org.apache.log4j.Level.TRACE);
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TestServerDlg.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TestServerDlg.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TestServerDlg.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TestServerDlg.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestServerDlg().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExit;
    private javax.swing.JLabel lblClientState;
    private javax.swing.JLabel lblClientStateTitle;
    // End of variables declaration//GEN-END:variables
}
