/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package src.main.java.model;

import src.main.java.controller.*;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Squee183
 */
public class ChatClient {

    private Controller clientController;

    private String username;
    private DatagramSocket socket;
    private InetSocketAddress trackerAddress;
    // private int trackerPort;
    private Map<InetAddress, Integer> activeClients = new HashMap<>();
    private volatile Boolean isRunning = true;
    private volatile Boolean isLogginIn = true;

    public ChatClient(Controller clientController, String username, InetAddress trackerAddress, int trackerPort)
            throws SocketException {
        this.clientController = clientController;
        // this.username = username;
        // this.trackerAddress = trackerAddress;
        // this.trackerPort = trackerPort;
        // this.socket = new DatagramSocket();
        // activeClients = new HashMap<>();
    }

    public ChatClient(Controller clientController) {
        this.clientController = clientController;
    }

    public void run() {
        this.clientController.toggleLoginVisible();

        try {
            while (this.isLogginIn) {
                // Wait for login from GUI
            }

            // send login message
            // InetAddress clientAddress = InetAddress.getLocalHost();
            // String loginMessage = String.format("LOGIN %s", username);
            // DatagramPacket logonPacket = new DatagramPacket(loginMessage.getBytes(),
            // loginMessage.getBytes().length, trackerAddress, trackerPort);
            // socket.send(logonPacket);

            // byte[] buffer = new byte[1024];
            // DatagramPacket loginResponsePacket = new DatagramPacket(buffer,
            // buffer.length);
            // socket.receive(loginResponsePacket);
            // String loginResponseMessage = new String(loginResponsePacket.getData(), 0,
            // loginResponsePacket.getLength());

            // receive login response, store peers in activeClients
            // if (loginResponseMessage.startsWith("USERS")) {
            // String[] parts = loginResponseMessage.split(" ");
            // for (int i = 0; i < parts.length; i++) {
            // String[] peersInfo = parts[i].split(":");
            // String clientAddressString = peersInfo[0];
            // int clientPort = Integer.parseInt(peersInfo[i]);
            // if (!clientAddressString.equals(clientAddress.getHostAddress())
            // || clientPort != socket.getLocalPort()) {
            // InetAddress address = InetAddress.getByName(clientAddressString);
            // activeClients.put(address, clientPort);
            // System.out.println(i + " Adding client: " + address + ":" + clientPort);
            // }
            // }
            // } else {
            // System.err.println("Login user response error. ");
            // System.exit(-1);
            // }

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

        if (loginResponseMessage.startsWith("USERS")) {
            String[] parts = loginResponseMessage.split(" ");
            for (int i = 0; i < parts.length; i++) {
                String[] peersInfo = parts[i].split(":");
                String clientAddressString = peersInfo[0];
                int clientPort = Integer.parseInt(peersInfo[i]);
                if (!clientAddressString.equals(clientAddress.getHostAddress())
                        || clientPort != socket.getLocalPort()) {
                    InetAddress address = InetAddress.getByName(clientAddressString);
                    activeClients.put(address, clientPort);
                    System.out.println(i + " Adding client: " + address + ":" + clientPort);
                }
            }
        } else {
            System.err.println("Login user response error. ");
            throw new Exception();
        }

        this.username = username;
        isLogginIn = false;
    }

    public void sendMessage(String message) {
        // handle logoff if client types exit
        if (message.equals(".")) {
            String logoffMessage = "LOGOFF " + username;
            DatagramPacket logoffPacket = new DatagramPacket(logoffMessage.getBytes(), logoffMessage.getBytes().length,
                    trackerAddress);
            try {
                socket.send(logoffPacket);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            return;
        }

        // send message to peers in activeClients
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formattedMessage = "MESSAGE " + username + " " + timestamp + " " + message;
        for (InetAddress address : activeClients.keySet()) {
            int port = activeClients.get(address);
            DatagramPacket sendPacket = new DatagramPacket(formattedMessage.getBytes(),
                    formattedMessage.getBytes().length, address, port);
            try {
                socket.send(sendPacket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class ListenerThread implements Runnable {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                while (true) {
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
                        InetAddress address = InetAddress.getByName(clientAddressStr);

                        if (activeClients.containsKey(address)) {
                            System.err.println("Attempted to add duplicate peer.");
                        } else {
                            activeClients.put(address, clientPort);
                            System.out.println("Added client: " + address + ":" + clientPort);
                        }

                    }

                    // for logoff packets, remove peer from activeClients
                    else if (header.equals("LOGOFF")) {
                        String[] addressParts = body.split(":");
                        String clientAddressStr = addressParts[0];
                        InetAddress address = InetAddress.getByName(clientAddressStr);
                        if (!activeClients.containsKey(address)) {
                            System.err.println("Attempted to remove duplicate peer not in active clients.");
                        } else {
                            activeClients.remove(address);
                            System.out.println("Removed client: " + address);
                        }
                    }

                    // display message from other peers
                    else {
                        System.out.println(name + ": " + body);
                    }

                    // reset the buffer and packet
                    buffer = new byte[1024];
                    packet = new DatagramPacket(buffer, buffer.length);
                }

            } catch (IOException e) {
                System.err.println("Error while listening for updates " + e.getMessage());
                isRunning = false;
            }

        }
    }

}
