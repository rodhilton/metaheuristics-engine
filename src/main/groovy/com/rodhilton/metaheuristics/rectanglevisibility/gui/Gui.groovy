package com.rodhilton.metaheuristics.rectanglevisibility.gui

import com.apple.eawt.Application
import com.google.common.base.Supplier
import com.rodhilton.metaheuristics.collections.ScoredSet
import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram
import com.rodhilton.metaheuristics.simulator.Simulator
import com.rodhilton.metaheuristics.simulator.SimulatorCallback

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.concurrent.atomic.AtomicInteger

public class Gui {

    private static void createAndShowGUI(AppState appState, int size) {
        JFrame frame = new JFrame()
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        appState.register(new AppStateListener() {
            @Override
            void updateState(AppState state) {
                frame.setTitle(state.title)
            }
        })

        ViewPanel panel = new ViewPanel(appState)

        panel.setPreferredSize(new Dimension(500, 500))
        frame.getContentPane().add(panel, BorderLayout.CENTER)

        JPanel toolBar = new JPanel();
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.LINE_AXIS));

        JButton playButton = new JButton("Play")
        JButton pauseButton = new JButton("Pause")

        playButton.addActionListener(new ActionListener() {

            @Override
            void actionPerformed(ActionEvent actionEvent) {
                playButton.setEnabled(false)
                pauseButton.setEnabled(true)
                appState.updatePaused(false)
            }
        })

        pauseButton.addActionListener(new ActionListener() {

            @Override
            void actionPerformed(ActionEvent actionEvent) {
                pauseButton.setEnabled(false)
                playButton.setEnabled(true)
                appState.updatePaused(true)
            }
        })

        playButton.setEnabled(false)
        toolBar.add(playButton)
        toolBar.add(pauseButton)

        JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL,size,1,1,size+1)
        toolBar.add(new JLabel("Level:"))
        toolBar.add(scrollBar)

        scrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
                println(adjustmentEvent.value)
                appState.updateCurrentRectangle(adjustmentEvent.value)
            }
        })

        frame.getContentPane().add(toolBar, BorderLayout.PAGE_END)

        frame.pack();

        //Center in screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize()
        frame.setLocation((int) (dim.width / 2 - frame.getSize().width / 2), (int) (dim.height / 2 - frame.getSize().height / 2))


        ArrayList<Image> icons = new ArrayList<Image>()
        icons.add(new ImageIcon(getClass().getResource("/icon16.png")).getImage());
        icons.add(new ImageIcon(getClass().getResource("/icon32.png")).getImage());
        def bigIcon = new ImageIcon(getClass().getResource("/icon64.png")).getImage()
        icons.add(bigIcon);
        frame.setIconImages(icons)

        Application application = Application.getApplication();
        application.setDockIconImage(bigIcon);

        frame.setVisible(true)

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            void componentResized(ComponentEvent componentEvent) {
                appState.updateSize(panel.size)
            }
        })
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        final int size = 15;
        final AppState appState = new AppState();
        appState.currRect=size
        appState.title="Rectangle Visibility"

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(appState, size);
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

        final AtomicInteger generation = new AtomicInteger()

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
                    simulator.stopSimulation();
                    appState.updateCompleted()
                }
            }
        }

        simulator.registerCallback(printer)
        simulator.registerCallback(stopper)

        simulator.startSimulation()

    }

}

