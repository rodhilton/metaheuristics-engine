package com.rodhilton.rectanglevisibility.network

import org.apache.activemq.ActiveMQConnectionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.jms.*

class MessageSender {

    private Session session
    private Connection connection
    private MessageProducer producer

    private DiagramMessage current = null

    private static Logger log = LoggerFactory.getLogger(MessageSender)

    public MessageSender(String server, int size) {
        def serverAddress = Messaging.getServerAddress(server)
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(serverAddress)

        connection = connectionFactory.createConnection()
        connection.start()

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

        Destination destination = session.createQueue("${Messaging.QUEUE_NAME}_${size}")

        producer = session.createProducer(destination)
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT)
        log.info("Sending messages to ${serverAddress}@${Messaging.QUEUE_NAME}_${size}...")
    }

    def sendMessage(DiagramMessage newest) {
        if (current == null || current.diagram == null || current.diagram.fitness() < newest.diagram.fitness()) {
            current = newest
            ObjectMessage os = session.createObjectMessage(newest)

            log.debug("Sent message: ${newest}")
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
