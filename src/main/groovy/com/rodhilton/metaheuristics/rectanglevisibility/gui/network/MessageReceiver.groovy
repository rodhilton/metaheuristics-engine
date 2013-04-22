package com.rodhilton.metaheuristics.rectanglevisibility.gui.network

import com.rodhilton.metaheuristics.rectanglevisibility.gui.AppState
import org.apache.activemq.ActiveMQConnectionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.jms.*

class MessageReceiver {
    private Connection connection
    private Session session
    private MessageConsumer consumer
    private boolean closed = false

    private static Logger log = LoggerFactory.getLogger(MessageSender)

    public MessageReceiver(String server) {
        def serverAddress = Messaging.getServerAddress(server)
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(serverAddress)

        connection = connectionFactory.createConnection()
        connection.start()

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

        Destination destination = session.createQueue("RectangleVisibility")

        consumer = session.createConsumer(destination)
        log.info("Listening for messages at ${serverAddress}...")
    }

    public void startReceive(AppState appState) {
        Thread t = new Thread() {
            public void run() {
                while (!closed) {
                    try {
                        if (appState.paused) {
                            try {
                                Thread.sleep(500)
                            } catch (InterruptedException e) {
                                log.error("Interrupted while sleeping in receiver pause loop", e)
                            }
                        }

                        Message message = consumer.receive()

                        if (message instanceof ObjectMessage) {
                            ObjectMessage os = (ObjectMessage) message;
                            DiagramMessage diagramMessage = (DiagramMessage) os.object;
                            log.info("Got message: ${diagramMessage}")
                            if (!appState.hasDiagram() || diagramMessage.diagram.fitness() > appState.getDiagram().fitness()) {
                                appState.updateDiagram(diagramMessage.diagram, diagramMessage.generationNum, diagramMessage.name)
                            }
                        }

                    } catch (JMSException e) {
                        log.error("Caught exception while receiving messages", e)
                    }
                }
            }
        }
        t.start()
    }

    public void close() {
        this.closed = true
        consumer.close();
        session.close();
        connection.close();
    }
}
