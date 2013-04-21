package com.rodhilton.metaheuristics.rectanglevisibility.gui.network

import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram
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
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Messaging.getServerAddress(server))

        connection = connectionFactory.createConnection()
        connection.start()

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

        Destination destination = session.createQueue("RectangleVisibility")

        consumer = session.createConsumer(destination)
    }

    public void startReceive(AppState appState) {
        Thread t = new Thread() {
            public void run() {
                while (!closed) {
                    try {
                        Message message = consumer.receive(1000)

                        if (message instanceof ObjectMessage) {
                            ObjectMessage os = (ObjectMessage) message;
                            VisibilityDiagram diagram = (VisibilityDiagram) os.object;
                            log.info("Got message ${diagram.fitness()}")
                            if (appState.diagramHistory.size() == 0 || diagram.fitness() > appState.diagramHistory.last().fitness()) {
                                appState.updateDiagram(diagram)
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
