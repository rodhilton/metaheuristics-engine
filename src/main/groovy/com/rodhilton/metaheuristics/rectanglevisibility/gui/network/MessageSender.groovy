package com.rodhilton.metaheuristics.rectanglevisibility.gui.network

import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram
import org.apache.activemq.ActiveMQConnectionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.jms.*

class MessageSender {

    private Session session
    private Connection connection
    private MessageProducer producer

    private VisibilityDiagram current = null

    private static Logger log = LoggerFactory.getLogger(MessageSender)

    public MessageSender(String server) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Messaging.getServerAddress(server))

        connection = connectionFactory.createConnection()
        connection.start()

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

        Destination destination = session.createQueue(Messaging.QUEUE_NAME)

        producer = session.createProducer(destination)
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT)
    }

    def sendDiagram(VisibilityDiagram newest) {
        if (current == null || current.fitness() < newest.fitness()) {
            current = newest
            ObjectMessage os = session.createObjectMessage(newest)

            log.debug("Sent message with fitness ${newest.fitness()}")
            try {
                producer.send(os);
            } catch (JMSException jmse) {
                log.error("Error while sending message", jmse)
            }
        }
    }

    def close() {
        producer.close();
        session.close();
        connection.close();
    }

}
