/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package src.main.java.model;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 *
 * @authors Alex Rodriguez, Mohammed Ibrahim, Christopher Gomez, Gaia Dennision
 */
public class Tracker {
    private int port;
    private DatagramSocket socket;
    private static Map<String, InetSocketAddress> peers = new HashMap<>();

    public Tracker(int port) {
        this.port = port;
    }

    public void start() {
        try {
            socket = new DatagramSocket(port);
            System.out.println("Tracker is running... " + socket.getLocalAddress() + ":" + socket.getLocalPort());

            while (true) {
                // Receive and process messages from clients
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                InetSocketAddress peerAddr = new InetSocketAddress(packet.getAddress(), packet.getPort());
                String message = new String(packet.getData(), 0, packet.getLength());
                processMessage(message, peerAddr);
            }
        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    private void processMessage(String message, InetSocketAddress address) {
        try {
            String[] tokens = message.split(" ");

            if (tokens[0].equals("LOGIN")) {
                String users = "USERS ";
                for (Map.Entry<String, InetSocketAddress> pair : peers.entrySet()) {
                    users += pair.getValue().getAddress().getHostAddress().split("/")[0] + ":"
                            + pair.getValue().getPort() + " ";
                }
                byte[] buff = users.getBytes();
                DatagramPacket packet = new DatagramPacket(buff, buff.length, address);
                socket.send(packet);

                String currentUser = "LOGIN " + tokens[1] + " " + address.getAddress().getHostAddress().split("/")[0]
                        + ":" + address.getPort();
                byte[] buff2 = currentUser.getBytes();

                for (Map.Entry<String, InetSocketAddress> pair : peers.entrySet()) {
                    packet = new DatagramPacket(buff2, buff2.length, pair.getValue());
                    socket.send(packet);
                }

                peers.put(tokens[1], address);

                System.out.println("User logged in - " + packet.getAddress().getHostAddress().split("/")[0] + " "
                        + address.getPort());

            } else if (tokens[0].equals("LOGOFF")) {
                peers.remove(tokens[1]);

                String currentUser = "LOGOFF " + tokens[1] + " " + address.getAddress().getHostAddress().split("/")[0]
                        + ":" + address.getPort();
                byte[] buff = currentUser.getBytes();
                for (Map.Entry<String, InetSocketAddress> pair : peers.entrySet()) {
                    DatagramPacket packet = new DatagramPacket(buff, buff.length, pair.getValue());
                    socket.send(packet);
                }

                System.out.println("User logged off - " + address.getAddress().getHostAddress().split("/")[0] + " "
                        + address.getPort());

            } else {
                // invalid
                System.out.println("Error: invalid input " + tokens[0]);
            }
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Tracker <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        Tracker tracker = new Tracker(port);
        tracker.start();
    }
}
