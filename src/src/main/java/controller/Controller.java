package src.main.java.controller;

import src.main.java.model.ChatClient;
import src.main.java.view.ClientGUI;
import src.main.java.view.LoginGUI;

public class Controller {
    private ChatClient model;
    private LoginGUI viewLogin;
    private ClientGUI viewChatClient;

    public Controller() {
        viewChatClient = new ClientGUI(this);
        viewLogin = new LoginGUI(this);
        this.model = new ChatClient(this);
    }

    public void handleMessageReceived(String message) {
        viewChatClient.addMessage(message);
    }

    public void handleMessageSent(String message) {
        model.sendMessage(message);
    }

    public void handleLoginMessage(String username, String ip, Integer port) {
        try {
            model.tryLoginInfo(username, ip, port);
        } catch (Exception e) {
            viewLogin.displayLoginError();
        }

    }

    public void toggleLoginVisible() {
        boolean isVisible = viewLogin.isVisible() ? false : true;
        viewLogin.setVisible(isVisible);
    }

    public void toggleClientVisible() {
        boolean isVisible = viewChatClient.isVisible() ? false : true;
        viewChatClient.setVisible(isVisible);
    }

    public void closeLogin() {
        viewLogin.close();
    }

    public void closeClient() {
        viewChatClient.close();
    }

    public static void main(String[] args) {
        Controller chatServerController = new Controller();
        chatServerController.model.run();
    }
}
