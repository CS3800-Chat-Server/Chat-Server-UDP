/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package src.main.java.model;

import src.main.java.controller.*;

import java.net.*;

/**
 *
 * @author Squee183
 */
public class ChatClient {

    private Controller clientController;

    private String username;
    private DatagramSocket socket;

    public ChatClient(Controller clientController) {
        this.clientController = clientController;
    }

    public void run() {

    }
}
