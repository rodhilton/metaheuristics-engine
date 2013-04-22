package com.rodhilton.rectanglevisibility.main

import com.google.common.base.Supplier
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.simulator.Simulator
import com.rodhilton.metaheuristics.simulator.SimulatorCallback
import com.rodhilton.rectanglevisibility.domain.VisibilityDiagram
import com.rodhilton.rectanglevisibility.gui.Gui
import com.rodhilton.rectanglevisibility.gui.TerminalInterface
import com.rodhilton.rectanglevisibility.network.DiagramMessage
import com.rodhilton.rectanglevisibility.network.MessageReceiver
import com.rodhilton.rectanglevisibility.network.MessageSender
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.swing.*
import java.util.concurrent.atomic.AtomicInteger

class Executive {

    private static Logger log = LoggerFactory.getLogger(Executive)

    public static void main(String[] args) {
        //Temporary.  If there are absolutely no args at all, it's in "demo mode" which means prompt for stuff
        if (args.size() == 0) {
            demoMode()
            System.exit(0)
        }

        ArgumentParser parser = ArgumentParsers.newArgumentParser("rectangles")
                .description("Metaheuristically search for a complete graph in a Rectangle Visibility Diagram")
                .epilog("Source code for this project available at:\n https://github.com/rodhilton/metaheuristics-engine")

        parser.addArgument("size")
                .dest("size")
                .type(Integer)
                .required(true)
                .help("The size of the complete graph being sought (the number of rectangles)")

        parser.addArgument("--mode")
                .dest("mode")
                .type(String)
                .choices("standalone", "client", "server")
                .setDefault("standalone")
                .help("The mode for the application. 'standalone' for normal operation, 'client' to send updates to a server, 'server' to recieve them (default: standalone)")

        parser.addArgument("--name")
                .dest("name")
                .type(String)
                .setDefault("Anon ${new Random().nextInt(1 << 30)}".toString())
                .help("Your name, only useful for client/server mode to identify source of a diagram")

        parser.addArgument("--server")
                .metavar("address[:port]")
                .dest("server")
                .type(String)
                .help("Server to send/receive updates via JMS. Required in server or client mode")

        parser.addArgument("--rng_seed")
                .dest("seed")
                .type(Long)
                .setDefault(System.currentTimeMillis())
                .help("Seed value to use for the random number generator")

        parser.addArgument("--gui")
                .dest("gui")
                .action(Arguments.storeTrue())
                .help("Whether or not to show the GUI")

        parser.addArgument("--verbose")
                .dest("verbose")
                .action(Arguments.storeTrue())
                .help("Turn debugging verbosity on")

        //TODO: option for resume from file or log (but not for server mode)
        //TODO: option for automatic saving (and how large the log should be)


        try {
            Namespace res = parser.parseArgs(args);

            //Extra error conditions
            if ((res.getString("mode") == "server" || res.getString("mode") == "client") && res.get("server") == null) {
                throw new ArgumentParserException("Must provide a server address running in client or server mode", parser);
            }

            //Somehow groovy's regex engine is different than what I'm expecting via ruby.  False negatives galore
//            if (!(/((?:localhost)|(?:\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}))(?:\:(\d{2,6}))?/ =~ res.get("server"))) {
//                throw new ArgumentParserException("Provided server address is not valid", parser);
//            }

            start(res.getInt("size"), res.getString("name"), res.getString("mode"), res.getString("server"), res.getBoolean("gui"), res.getLong("seed"), res.getBoolean("verbose"))
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

    public static start(int size, String name, String mode, String server, boolean gui, long seed, boolean verbose) {
        final AppState appState = new AppState();
        appState.currRect = size
        appState.maxRect = size
        appState.title = "Rectangle Visibility [${mode.toLowerCase()}]"
        appState.name = name

        if (gui) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Gui.createAndShowGUI(appState);
                }
            });
        } else {
            TerminalInterface.launchInterface(appState, System.out, verbose)
        }

        if (verbose) {
            def debuggingListener = new AppStateListener() {

                @Override
                void updateState(AppState state) {
                    def (List visiblePairs, List invisiblePairs) = state.getDiagram().getPairs()
                    System.err.println("Generation: ${state.currentGeneration()}, Fitness: ${state.diagram.fitness()}")
                    System.err.println("Visible pairs: " + visiblePairs.collect { "(${it.bottomIndex}, ${it.topIndex})" }.join(", "))
                    System.err.println("Invisible pairs: " + invisiblePairs.collect { "(${it.bottomIndex}, ${it.topIndex})" }.join(", "))
                }

            }
            appState.register(debuggingListener)
        }

        if (mode == "server") {
            MessageReceiver networkReciever = new MessageReceiver(server, size)
            networkReciever.startReceive(appState)
        } else {
            Random random = new Random(seed)
            log.info("Random seed: ${seed}")

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
                        appState.updateCompleted()
                    }
                }
            }

            if (mode == "client") {

                MessageSender networkSender = new MessageSender(server, size)

                def networkingListener = new AppStateListener() {
                    @Override
                    void updateState(AppState state) {
                        if (state.hasDiagram()) {
                            if (state.completed) {
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
            }

            simulator.registerCallback(printer)
            simulator.registerCallback(stopper)

            simulator.startSimulation()
        }
    }

    /**
     * This prompts the user for required inputs and launches the app with the gui, in client mode.  It is here
     * To make it so students can double-click on the run-script and appropriately launch the right mode of the app
     * for the presentation.  To be removed after presentations
     */
    public static void demoMode() {
        Integer size = null
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

        String name = null
        while (name == null || name.trim() == "") {
            println("Enter your name:")
            print("> ")
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            name = bufferRead.readLine().trim();
        }

        String server = null
        while (server == null || server.trim() == "") {
            println("Enter the server to connect to:")
            print("> ")
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            server = bufferRead.readLine().trim();
        }

        start(size, name, "client", server, true, System.currentTimeMillis(), false)
    }
}
