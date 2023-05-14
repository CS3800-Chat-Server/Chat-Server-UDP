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
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @authors Alex Rodriguez, Mohammed Ibrahim, Christopher Gomez, Gaia Dennision
 */
public class ChatBot implements Runnable {
    private static ConcurrentHashMap<InetSocketAddress, Integer> peers = new ConcurrentHashMap<InetSocketAddress, Integer>();
    private DatagramSocket socket;
    InetSocketAddress addr;
    private volatile boolean isRunning = true;
    private int id;
    private String username;

    public ChatBot(String ip, int port, int id) {
        addr = new InetSocketAddress(ip, port);
        try {
            this.socket = new DatagramSocket();
            System.out.println("Bot" + id + " at " + addr.getAddress().getHostAddress() + ":" + addr.getPort());
            socket.setSoTimeout(2000);
            this.id = id;
        } catch (SocketException e) {
            System.err.println("ChatBot() Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        username = "Bot" + id;
        String loginString = "LOGIN " + username;

        byte[] buff = loginString.getBytes();
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
                    InetSocketAddress addr = new InetSocketAddress(userInfo[0].split("/")[0], Integer.parseInt(userInfo[1]));
                    peers.put(addr, Integer.parseInt(userInfo[1]));
                }
            }

            BotOutput botOutput = new BotOutput();
            new Thread(botOutput).start();

            while (isRunning) {
                buff = new byte[1024];
                DatagramPacket messagePacket = new DatagramPacket(buff, buff.length);

                try {
                    socket.receive(messagePacket);
                } catch (SocketTimeoutException e) {
                    continue;
                }                

                String message = new String(messagePacket.getData(), 0, messagePacket.getLength());
                String[] messageTokens = message.split(" ");

                if (messageTokens[0].equals("MESSAGE")) {
                } else if (messageTokens[0].equals("LOGIN")) {
                    String[] userInfo2 = messageTokens[2].split(":");
                    InetSocketAddress addr = new InetSocketAddress(userInfo2[0], Integer.parseInt(userInfo2[1]));
                    peers.put(addr, Integer.parseInt(userInfo2[1]));
                } else if (messageTokens[0].equals("LOGOFF")) {
                    String[] userInfo2 = messageTokens[2].split(":");
                    InetSocketAddress addr = new InetSocketAddress(userInfo2[0], Integer.parseInt(userInfo2[1]));
                    peers.remove(addr);
                }
            }

            String logoffString = "LOGOFF " + username;
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
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String output = "MESSAGE " + username + " " + timestamp + " " + "This is a message from " + username;
                byte[] buffer = output.getBytes();
                for (ConcurrentHashMap.Entry<InetSocketAddress, Integer> pair : peers.entrySet()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, pair.getKey());
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
            ChatBot bot = new ChatBot(ip, port, i+1);
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
