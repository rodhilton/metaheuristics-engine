package com.rodhilton.metaheuristics.rectanglevisibility.gui.network

class Messaging {
    static String QUEUE_NAME = "RectangleVisibility"
    static int DEFAULT_PORT = 61616

    static String getServerAddress(String server) {
        String s = server.replaceAll(/\s+/, "")
        if(s.contains(":")) {
            return "tcp://${s}"
        } else {
            return "tcp://${s}:${61616}"
        }
    }
}
