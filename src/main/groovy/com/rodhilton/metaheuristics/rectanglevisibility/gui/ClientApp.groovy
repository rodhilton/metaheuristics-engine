package com.rodhilton.metaheuristics.rectanglevisibility.gui

import com.google.common.base.Supplier
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram
import com.rodhilton.metaheuristics.rectanglevisibility.gui.network.DiagramMessage
import com.rodhilton.metaheuristics.rectanglevisibility.gui.network.MessageSender
import com.rodhilton.metaheuristics.simulator.Simulator
import com.rodhilton.metaheuristics.simulator.SimulatorCallback

import javax.swing.*
import java.util.concurrent.atomic.AtomicInteger

class ClientApp {
    private Integer size
    private String name
    private String server

    public ClientApp(Integer size, String name, String server) {
        this.size = size
        this.name = name
        this.server = server
    }

    public void launch() {
        final AppState appState = new AppState();
        appState.currRect = size
        appState.title = "Rectangle Visibility - Client"
        appState.maxRect = size
        appState.name = name

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Gui.createAndShowGUI(appState);
            }
        });

        Random random = new Random()

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
        appState.register(pauseListener)

        final AtomicInteger generation = new AtomicInteger();

        SimulatorCallback<VisibilityDiagram> printer = new SimulatorCallback<VisibilityDiagram>() {
            @Override
            void call(ScoredSet<VisibilityDiagram> everything) {
                VisibilityDiagram best = everything.getBest()
                appState.updateDiagram(best, generation.incrementAndGet(), name)
            }
        }

        SimulatorCallback<VisibilityDiagram> stopper = new SimulatorCallback<VisibilityDiagram>() {
            @Override
            void call(ScoredSet<VisibilityDiagram> everything) {
                VisibilityDiagram best = everything.getBest()
                if (best.fitness() >= (size * (size - 1)) / 2) {
                    simulator.stopSimulation()
                    appState.updateCompleted()
                }
            }
        }

        MessageSender networkSender = new MessageSender(server)

        def networkingListener = new AppStateListener() {
            @Override
            void updateState(AppState state) {
                if (state.hasDiagram()) {
                    if(state.completed) {
                        networkSender.close()
                    }

                    networkSender.sendMessage(new DiagramMessage(
                            diagram: state.getDiagram(),
                            name: state.name,
                            generationNum: state.currentGeneration()
                    ))
                }
            }
        }

        appState.register(networkingListener)

        simulator.registerCallback(printer)
        simulator.registerCallback(stopper)

        simulator.startSimulation()
    }

    public static void main(String[] args) {

        Integer size = (args as ArrayList<String>)[0]?.toInteger()
        while (size == null) {
            println("Enter the number of vertices/rectangles you're looking for:")
            print("> ")
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String s = bufferRead.readLine();
            try {
                size = s.toInteger()
            } catch (NumberFormatException e) {
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

        String server = (args as ArrayList<String>)[2]
        while (server == null || server.trim() == "") {
            println("Enter the server to connect to:")
            print("> ")
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            server = bufferRead.readLine().trim();
        }

        ClientApp app = new ClientApp(size, name, server)
        app.launch()

    }
}