/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package src.main.java.view;

import javax.swing.JOptionPane;

import src.main.java.controller.*;

/**
 *
 * @author Squee183
 */
public class ClientGUI extends javax.swing.JFrame {

    private Controller clientHandler;

    /**
     * Creates new form ClientGUI
     */
    public ClientGUI(Controller clientController) {
        this.clientHandler = clientController;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chatLog = new java.awt.List();
        userInput = new java.awt.TextField();
        sendChatButton = new javax.swing.JButton();
        applicationLabel = new java.awt.Label();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(102, 102, 102));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                int option = JOptionPane.showConfirmDialog(ClientGUI.this, "Are you sure you want to exit?",
                        "Confrim Exit",
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    // If the user confirms, dispose the window
                    close();
                }
            }
        });

        userInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                userInputKeyPressed(evt);
            }
        });

        sendChatButton.setText("Send");
        sendChatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendChatButtonActionPerformed(evt);
            }
        });

        applicationLabel.setText("CS3800 Chat Server");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(chatLog, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(applicationLabel,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 130,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(userInput,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 439,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(sendChatButton)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(applicationLabel, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chatLog, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(userInput, javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sendChatButton, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addContainerGap()));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void userInputKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_userInputKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            String text = userInput.getText();
            if (text.length() == 0) {
                userInput.setText("");
                return;
            }
            // Send to controller to send to server
            clientHandler.handleMessageSent(text);
            userInput.setText("");
        }

    }// GEN-LAST:event_userInputKeyPressed

    private void sendChatButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_sendChatButtonActionPerformed
        String text = userInput.getText();
        if (text.length() == 0) {
            userInput.setText("");
            return;
        }

        // Send to controller to send to server
        clientHandler.handleMessageSent(text);
        userInput.setText("");

    }// GEN-LAST:event_sendChatButtonActionPerformed

    public void addMessage(String message) {
        chatLog.add(message);
    }

    public void close() {
        clientHandler.handleMessageSent(".");
        this.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Label applicationLabel;
    private java.awt.List chatLog;
    private javax.swing.JButton sendChatButton;
    private java.awt.TextField userInput;
    // End of variables declaration//GEN-END:variables
}
