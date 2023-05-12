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
    private String response;

    public Tracker (int port) {
        this.port = port;
    }
    
    public void start() {
        try {
            socket = new DatagramSocket(port);
            System.out.println("Tracker is running...");

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
            e.printStackTrace();
        }
    }

    private void processMessage(String message, InetSocketAddress address) {
        try {
            String[] tokens = message.split(" ");
            
            if(tokens[0].equals("LOGIN")) {
                String users = "USERS ";
                for(Map.Entry<String, InetSocketAddress> pair : peers.entrySet()) {
                    users += pair.getValue().getAddress() + ":" + pair.getValue().getPort() + " ";
                }
                byte[] buff = users.getBytes();
                DatagramPacket packet = new DatagramPacket(buff, buff.length, address);
                socket.send(packet);

                peers.put(tokens[1], address);
                String currentUser = "LOGIN " + tokens[1] + " " + address.getAddress() + ":" + address.getPort();
                buff = currentUser.getBytes();

                for(Map.Entry<String, InetSocketAddress> pair : peers.entrySet()) {
                    if(address != pair.getValue()){
                        packet = new DatagramPacket(buff, buff.length, pair.getValue());//, pair.getValue().getPort());
                        socket.send(packet);
                    } else {
                        continue;
                    }
                }
            } else if(tokens[0].equals("LOGOFF")) {
                peers.remove(tokens[1]);

                String currentUser = "LOGOFF " + tokens[1] + " " + address.getAddress() + ":" + address.getPort();
                byte[] buff = currentUser.getBytes();
                for(Map.Entry<String, InetSocketAddress> pair : peers.entrySet()) {
                    DatagramPacket packet = new DatagramPacket(buff, buff.length, pair.getValue());
                    socket.send(packet);
                }
            } else {
                // invalid
                System.out.println("Error: invalid input " + tokens[0]);
            }
        } catch (Exception e) {
            // TODO: handle exception
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
