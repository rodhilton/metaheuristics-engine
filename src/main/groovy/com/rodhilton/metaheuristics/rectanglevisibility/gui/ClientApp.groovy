package com.rodhilton.metaheuristics.rectanglevisibility.gui

import com.google.common.base.Supplier
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram
import com.rodhilton.metaheuristics.simulator.Simulator
import com.rodhilton.metaheuristics.simulator.SimulatorCallback
import org.apache.activemq.ActiveMQConnection
import org.apache.activemq.ActiveMQConnectionFactory

import javax.jms.*
import javax.swing.*

class ClientApp {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        Integer size = (args as ArrayList<String>)[0]?.toInteger()
        while (size == null) {
            println("Enter the number of vertices/rectangles you're looking for:")
            print("> ")
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String s = bufferRead.readLine();
            try {
                size = s.toInteger()
            } catch (Exception) {
                //Ignore, prompt again
            }
        }

        String name = (args as ArrayList<String>)[1]
        while (name == null || name.trim() == "") {
            println("Enter your name:")
            print("> ")
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            name = bufferRead.readLine().trim();
        }

        final AppState appState = new AppState();
        appState.currRect = size
        appState.title = "Rectangle Visibility"
        appState.maxRect = size
        appState.name = name

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Gui.createAndShowGUI(appState);
            }
        });

        long seed = System.currentTimeMillis()
//        long seed = 1366305638180L
        println("RNG Seed: " + seed)
        Random random = new Random(seed)
//        random.setSeed(12345L);
        final Simulator simulator = new Simulator(new Supplier<VisibilityDiagram>() {

            @Override
            VisibilityDiagram get() {
                return new VisibilityDiagram(size, random);
            }
        })

        def pauseListener = new AppStateListener() {
            @Override
            void updateState(AppState state) {
                if (!simulator.paused && state.paused) {
                    simulator.pauseSimulation()
                } else if (simulator.paused && !state.paused) {
                    simulator.unpauseSimulation()
                }
            }
        }

        NetworkSender networkSender = new NetworkSender()

        def messageSendingListener = new AppStateListener() {
            @Override
            void updateState(AppState state) {
                if (state.diagramHistory.size() > 0) {
                    networkSender.sendDiagram(state.diagramHistory.last())
                }
            }
        }

        appState.register(pauseListener)
        appState.register(messageSendingListener)

        SimulatorCallback<VisibilityDiagram> printer = new SimulatorCallback<VisibilityDiagram>() {
            @Override
            void call(ScoredSet<VisibilityDiagram> everything) {
                VisibilityDiagram best = everything.getBest()
                appState.updateDiagram(best)
            }
        }

        SimulatorCallback<VisibilityDiagram> stopper = new SimulatorCallback<VisibilityDiagram>() {

            @Override
            void call(ScoredSet<VisibilityDiagram> everything) {
                VisibilityDiagram best = everything.getBest()
                if (best.fitness() >= (size * (size - 1)) / 2) {
                    simulator.stopSimulation()
                    appState.updateCompleted()
                    networkSender.close()
                }
            }
        }


        simulator.registerCallback(printer)
        simulator.registerCallback(stopper)

        simulator.startSimulation()

    }
}


class NetworkSender {

    private Session session
    private Connection connection
    private MessageProducer producer

    private VisibilityDiagram current = null

    public NetworkSender() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);

        // Create a Connection
        connection = connectionFactory.createConnection();
        connection.start();

        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue("RectangleVisibility")

        // Create a MessageProducer from the Session to the Topic or Queue
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }

    def sendDiagram(VisibilityDiagram newest) {

        if (current == null || current.fitness() < newest.fitness()) {
            current = newest
            def blah = new Thread() {
                @Override
                public void run() {
                    // Create a messages
                    ObjectMessage os = session.createObjectMessage(newest)

                    // Tell the producer to send the message
                    System.out.println("Sent message with fitness ${newest.fitness()}")
                    try {
                        producer.send(os);
                    } catch (JMSException jmse) {
                        jmse.printStackTrace()
                        //Ignore, they can be missed
                    }
                }
            }
            blah.start()
        }
    }

    def close() {
        session.close();
        connection.close();
    }
}