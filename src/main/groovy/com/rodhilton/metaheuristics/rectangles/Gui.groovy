package com.rodhilton.metaheuristics.rectangles

import com.google.common.base.Supplier
import com.rodhilton.metaheuristics.GraphThing
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.simulator.Simulator
import com.rodhilton.metaheuristics.simulator.SimulatorCallback

import javax.swing.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage
import java.util.List
import java.util.concurrent.atomic.AtomicInteger

class GraphThingPanel extends JPanel {
    AppState state;

    public GraphThingPanel(AppState state) {
        this.state = state
        state.registerListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage buff=this.state.graphThing.render((int)this.state.size.width, (int)this.state.size.height)
        g.drawImage(buff, 0, 0, null)
    }


    void updateState(AppState state) {
        this.state = state
        this.updateUI()
    }

}

class Gui {

    private static void createAndShowGUI(AppState appState) {
        //Create and set up the window.
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        JLabel emptyLabel = new JLabel("");
        GraphThingPanel panel = new GraphThingPanel(appState)

        panel.setPreferredSize(new Dimension(500, 500));
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((int) (dim.width / 2 - frame.getSize().width / 2), (int) (dim.height / 2 - frame.getSize().height / 2));

        frame.setVisible(true);

        frame.addComponentListener(new ComponentAdapter() {

            @Override
            void componentResized(ComponentEvent componentEvent) {
                appState.updateSize(componentEvent.component.size)
            }
        })
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        final AppState state = new AppState();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(state);
            }
        });


        final int size = 15;
        long seed = System.currentTimeMillis()
//        long seed = 1366305638180L
        println("RNG Seed: " + seed)
        Random random = new Random(seed);
//        random.setSeed(12345L);
        final Simulator simulator = new Simulator(new Supplier<GraphThing>() {

            @Override
            GraphThing get() {
                return new GraphThing(size, random);
            }
        })

        final AtomicInteger generation = new AtomicInteger()

        SimulatorCallback<GraphThing> printer = new SimulatorCallback<GraphThing>() {
            @Override
            void call(ScoredSet<GraphThing> everything) {
                GraphThing best = everything.getBest()
                //println("${generation.incrementAndGet()}: ${best.fitness()}/${(size * (size - 1)) / 2}")
                state.updateGraphThing(best)
            }
        }

        SimulatorCallback<GraphThing> stopper = new SimulatorCallback<GraphThing>() {

            @Override
            void call(ScoredSet<GraphThing> everything) {
                GraphThing best = everything.getBest()
                if (best.fitness() >= (size * (size - 1)) / 2) {
                    simulator.stopSimulation();
                }
            }
        }

        simulator.registerCallback(printer);
        simulator.registerCallback(stopper);

        simulator.startSimulation();

    }

}

class AppState {
    GraphThing graphThing
    Dimension size
    List<GraphThingPanel> panels = new ArrayList<GraphThingPanel>()

    void updateGraphThing(GraphThing graphThing) {
        this.graphThing=graphThing
        notifyListeners();
    }

    void updateSize(Dimension newSize) {
        this.size = newSize;
        notifyListeners()
    }

    void registerListener(GraphThingPanel panel) {
        panels.add(panel)
    }

    void notifyListeners() {
        for (GraphThingPanel panel : panels) {
            panel.updateState(this)
        }
    }
}
