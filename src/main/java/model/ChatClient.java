/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package src.main.java.model;

import src.main.java.controller.*;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @authors Alex Rodriguez, Mohammed Ibrahim, Christopher Gomez, Gaia Dennision
 */
public class ChatClient {

    private Controller clientController;

    private String username;
    private DatagramSocket socket;
    private InetSocketAddress trackerAddress;
    private ConcurrentHashMap<InetSocketAddress, Integer> activeClients = new ConcurrentHashMap<>();
    private volatile Boolean isRunning = true;
    private volatile Boolean isLogginIn = true;

    public ChatClient(Controller clientController) {
        this.clientController = clientController;
    }

    public void run() {
        this.clientController.toggleLoginVisible();

        try {
            while (this.isLogginIn) {
                // Wait for login from GUI
            }

            this.clientController.closeLogin();
            this.clientController.toggleClientVisible();

            // start thread to listen for messages
            Thread listenerThread = new Thread(new ListenerThread());
            listenerThread.start();

            // send messages
            while (isRunning) {
                /**
                 * Server will stay running until user inputs "."
                 * and ClientListener recieves sign off confirmation "BYE"
                 */
            }

            this.clientController.closeClient();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

    }

    public void tryLoginInfo(String username, String ip, Integer port) throws Exception {
        InetAddress clientAddress = InetAddress.getLocalHost();
        trackerAddress = new InetSocketAddress(ip, port);
        try {
            this.socket = new DatagramSocket();
            String loginMessage = String.format("LOGIN %s", username);
            DatagramPacket logonPacket = new DatagramPacket(loginMessage.getBytes(), loginMessage.getBytes().length,
                    trackerAddress);
            socket.send(logonPacket);
        } catch (IOException e) {
            throw e;
        }

        byte[] buffer = new byte[1024];
        DatagramPacket loginResponsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(loginResponsePacket);
        String loginResponseMessage = new String(loginResponsePacket.getData(), 0, loginResponsePacket.getLength());
        String[] parts = loginResponseMessage.split(" ");

        if (parts[0].equals("USERS")) {
            for (int i = 1; i < parts.length; i++) {
                String[] peersInfo = parts[i].split(":");
                String clientAddressString = peersInfo[0];
                int clientPort = Integer.parseInt(peersInfo[i]);
                if (!clientAddressString.equals(clientAddress.getHostAddress())
                        || clientPort != socket.getLocalPort()) {
                    InetSocketAddress address = new InetSocketAddress(clientAddressString, clientPort);
                    activeClients.put(address, clientPort);
                }
            }
        } else {
            throw new Exception();
        }

        this.username = username;
        isLogginIn = false;
    }

    public void sendMessage(String message) {
        // handle logoff if client types exit
        if (message.equals(".") && isRunning) {
            String logoffMessage = "LOGOFF " + username;
            DatagramPacket logoffPacket = new DatagramPacket(logoffMessage.getBytes(), logoffMessage.getBytes().length,
                    trackerAddress);
            try {
                socket.send(logoffPacket);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            isRunning = false;
            return;
        } else if (isRunning) {
            // send message to peers in activeClients
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String formattedMessage = "MESSAGE " + username + " " + timestamp + " " + message;
            for (InetSocketAddress address : activeClients.keySet()) {
                DatagramPacket sendPacket = new DatagramPacket(formattedMessage.getBytes(),
                        formattedMessage.getBytes().length, address);
                try {
                    socket.send(sendPacket);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }

            clientController.handleMessageReceived(username + " " + timestamp + " " + message);
        }   
    }

    private class ListenerThread implements Runnable {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                while (isRunning) {
                    // receive packets
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = message.split(" ", 3); // get header and body
                    String header = parts[0];
                    String name = parts[1];
                    String body = parts[2];

                    // for login packets, add peer to activeClients
                    if (header.equals("LOGIN")) {
                        String[] addressParts = body.split(":");
                        String clientAddressStr = addressParts[0];
                        int clientPort = Integer.parseInt(addressParts[1]);
                        InetSocketAddress address = new InetSocketAddress(clientAddressStr, clientPort);

                        if (activeClients.containsKey(address)) {
                            System.err.println("Attempted to add duplicate peer.");
                        } else {
                            activeClients.put(address, clientPort);
                            clientController.handleMessageReceived(name + " joined the chat.");
                        }

                    }

                    // for logoff packets, remove peer from activeClients
                    else if (header.equals("LOGOFF")) {
                        String[] addressParts = body.split(":");
                        String clientAddressStr = addressParts[0];
                        Integer clientPort = Integer.parseInt(addressParts[1]);
                        InetSocketAddress address = new InetSocketAddress(clientAddressStr, clientPort);
                        if (!activeClients.containsKey(address)) {
                            System.err.println("Attempted to remove duplicate peer not in active clients.");
                        } else {
                            activeClients.remove(address);
                            clientController.handleMessageReceived(name + " left the chat.");
                        }
                    }

                    // display message from other peers
                    else if (header.equals("MESSAGE")) {
                        clientController.handleMessageReceived(name + ": " + body);
                    }

                    // reset the buffer and packet
                    buffer = new byte[1024];
                    packet = new DatagramPacket(buffer, buffer.length);
                }

            } catch (IOException e) {
                isRunning = false;
            }

        }
    }

}
