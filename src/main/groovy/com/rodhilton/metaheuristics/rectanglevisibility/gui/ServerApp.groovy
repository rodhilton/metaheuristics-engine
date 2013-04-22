package com.rodhilton.metaheuristics.rectanglevisibility.gui

import com.rodhilton.metaheuristics.rectanglevisibility.gui.network.MessageReceiver

import javax.swing.*

class ServerApp {
    private String server

    public ServerApp(String server) {
        this.server = server
    }

    public launch() {
        final AppState appState = new AppState();
        appState.title = "Rectangle Visibility - Server"

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Gui.createAndShowGUI(appState);
            }
        });

        MessageReceiver networkReciever = new MessageReceiver(server)
        networkReciever.startReceive(appState)
    }

    public static void main(String[] args) {
        String server = (args as ArrayList<String>)[0]
        while (server == null || server.trim() == "") {
            println("Enter the server to connect to:")
            print("> ")
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            server = bufferRead.readLine().trim();
        }

        ServerApp serverApp = new ServerApp(server)
        serverApp.launch()
    }
}
