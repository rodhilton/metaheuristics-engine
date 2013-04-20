package com.rodhilton.metaheuristics.rectanglevisibility.gui

import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram
import org.apache.activemq.ActiveMQConnection
import org.apache.activemq.ActiveMQConnectionFactory

import javax.jms.*
import javax.swing.*

class ServerApp {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        final AppState appState = new AppState();
        appState.title = "Rectangle Visibility - Server"

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Gui.createAndShowGUI(appState);
            }
        });

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);

        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue("RectangleVisibility")

        // Create a MessageConsumer from the Session to the Topic or Queue
        MessageConsumer consumer = session.createConsumer(destination);

        Thread t = new Thread() {


            public void run() {
                while (true) {
                    // Create a ConnectionFactory
                    try {
                        // Wait for a message
                        Message message = consumer.receive(1000);

                        if (message instanceof ObjectMessage) {
                            ObjectMessage os = (ObjectMessage) message;
                            VisibilityDiagram diagram = (VisibilityDiagram) os.object;
                            if(appState.diagramHistory.size() == 0 || diagram.fitness() > appState.diagramHistory.last().fitness()) {
                                appState.updateDiagram(diagram)
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Caught: " + e);
                        e.printStackTrace();
                    }
                }
            }
        }
        t.start()

//            consumer.close();
//            session.close();
//            connection.close();

    }
}
