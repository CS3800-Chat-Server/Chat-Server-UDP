/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package src.main.java.tool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Squee183
 */
public class ChatBot implements Runnable {
    private static HashMap<String, Integer> peers = new HashMap<String, Integer>();
    private DatagramSocket socket;
    InetSocketAddress addr;
    private volatile boolean isRunning = true;
    private int id;

    public ChatBot(String ip, int port, int id) {
        addr = new InetSocketAddress(ip, port);
        try {
            this.socket = new DatagramSocket(277 + id);
            this.id = id;
        } catch (SocketException e) {
            System.err.println("ChatBot() Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String username = "Bot " + id;

        byte[] buff = username.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(buff, buff.length, addr);

        try {
            socket.send(loginPacket);

            buff = new byte[1024];
            DatagramPacket usersPacket = new DatagramPacket(buff, buff.length);

            socket.receive(usersPacket);

            String userList = new String(usersPacket.getData(), 0, usersPacket.getLength());
            String[] tokens = userList.split(" ");

            for (String s : tokens) {
                if (!s.equals("USERS")) {
                    String[] userInfo = s.split(":");
                    peers.put(userInfo[0], Integer.parseInt(userInfo[1]));
                }
            }

            BotOutput botOutput = new BotOutput();
            new Thread(botOutput).start();

            while (isRunning) {
                buff = new byte[1024];
                DatagramPacket messagePacket = new DatagramPacket(buff, buff.length);

                socket.receive(messagePacket);

                String message = new String(messagePacket.getData(), 0, messagePacket.getLength());
                String[] messageTokens = message.split(" ");

                if (messageTokens[0].equals("MESSAGE")) {
                    System.out.println(message);
                } else if (messageTokens[0].equals("LOGIN")) {
                    String[] userInfo2 = messageTokens[2].split(":");
                    peers.put(userInfo2[0], Integer.parseInt(userInfo2[1]));
                } else if (messageTokens[0].equals("LOGOFF")) {
                    String[] userInfo2 = messageTokens[2].split(":");
                    peers.remove(userInfo2[0]);
                }
            }

            String logoffString = "LOGOUT " + username;
            byte[] buffer = logoffString.getBytes();
            DatagramPacket logoffPacket = new DatagramPacket(buffer, buffer.length, addr);
            socket.send(logoffPacket);

            socket.close();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println(e.getStackTrace());
            isRunning = false;
            System.exit(1);
        }
    }

    private class BotOutput implements Runnable {

        public BotOutput() {

        }

        @Override
        public void run() {
            while (isRunning) {
                String output = "This is a message from bot " + id;
                byte[] buffer = output.getBytes();
                for (HashMap.Entry<String, Integer> pair : peers.entrySet()) {
                    InetSocketAddress peerAddr = new InetSocketAddress(pair.getKey(), pair.getValue());
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, peerAddr);
                    try {
                        socket.send(packet);
                    } catch (IOException e) {
                        System.err.println("Could not send message: " + output);
                        System.err.println("Error: " + e.getMessage());
                        System.err.println(e.getStackTrace());
                        isRunning = false;
                        System.exit(1);
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.err.println("Error: " + e.getMessage());
                    System.err.println(e.getStackTrace());
                    isRunning = false;
                    System.exit(1);
                }

            }
        }
    }

    public static void main(String[] args) {
        int numClients;
        int port;
        String ip;

        if (args.length != 3) {
            System.err.println("Usage: <ip-address> <port> <number of clients>");
            return;
        }

        ip = args[0];
        port = Integer.parseInt(args[1]);
        numClients = Integer.parseInt(args[2]);

        ArrayList<ChatBot> userArray = new ArrayList<>();

        for (int i = 0; i < numClients; i++) {
            ChatBot bot = new ChatBot(ip, port, i);
            userArray.add(bot);
            new Thread(bot).start();
            try {
                TimeUnit.MILLISECONDS.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // To stop the bots we can input a keyword or just wait for any input
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        for (ChatBot bot : userArray) {
            bot.isRunning = false;
        }

        scanner.close();
    }

}
