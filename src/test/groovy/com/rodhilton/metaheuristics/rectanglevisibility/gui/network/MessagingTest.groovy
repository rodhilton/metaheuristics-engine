package com.rodhilton.metaheuristics.rectanglevisibility.gui.network

import org.junit.Test

class MessagingTest {

    @Test
    void leavesAddressWithPortAlone() {
        assert Messaging.getServerAddress("localhost:6777") == "tcp://localhost:6777"
    }

    @Test
    void stripsUselessSpaces() {
        assert Messaging.getServerAddress(" localhost : 6777 ") == "tcp://localhost:6777"
    }

    @Test
    void defaultPortIfNotProvided() {
        assert Messaging.getServerAddress("localhost") == "tcp://localhost:${Messaging.DEFAULT_PORT}"
    }
}
