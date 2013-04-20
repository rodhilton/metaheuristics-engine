package com.rodhilton.metaheuristics.rectanglevisibility.gui

import javax.swing.*
import java.awt.*
import java.awt.event.*

public class Gui {

    public static void createAndShowGUI(AppState appState) {
        JFrame frame = new JFrame()
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

        ViewPanel panel = new ViewPanel(appState)

        panel.setPreferredSize(new Dimension(500, 500))
        frame.getContentPane().add(panel, BorderLayout.CENTER)

        JPanel toolBar = new JPanel();
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.LINE_AXIS));

        JButton playButton = new JButton("Play")
        JButton pauseButton = new JButton("Pause")
        playButton.setEnabled(false)

        toolBar.add(playButton)
        toolBar.add(pauseButton)

        JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL)
        scrollBar.setEnabled(false)
        toolBar.add(new JLabel("Level:"))
        toolBar.add(scrollBar)

        JCheckBox labelCheckBox = new JCheckBox("Labels:", true)
        labelCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
        toolBar.add(labelCheckBox)

        JCheckBox hoverCheckBox = new JCheckBox("Hover:", false)
        hoverCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
        toolBar.add(hoverCheckBox)

        JButton exportButton = new JButton("Export")
        toolBar.add(exportButton)

        frame.getContentPane().add(toolBar, BorderLayout.PAGE_START)
        frame.pack();

        //Center in screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize()
        frame.setLocation((int) (dim.width / 2 - frame.getSize().width / 2), (int) (dim.height / 2 - frame.getSize().height / 2))

        ArrayList<Image> icons = new ArrayList<Image>()
        icons.add(new ImageIcon(getClass().getResource("/icon16.png")).getImage());
        icons.add(new ImageIcon(getClass().getResource("/icon32.png")).getImage());
        icons.add(new ImageIcon(getClass().getResource("/icon64.png")).getImage());
        icons.add(new ImageIcon(getClass().getResource("/icon128.png")).getImage());
        def bigIcon = new ImageIcon(getClass().getResource("/icon256.png")).getImage()
        icons.add(bigIcon);
        frame.setIconImages(icons)

        try {
            Object appleApplication = Class.forName("com.apple.eawt.Application").newInstance()
            def application = (com.apple.eawt.Application)appleApplication
            application.setDockIconImage(bigIcon);
        } catch(ClassNotFoundException e) {
            //Ignore, just means we're not running on an Apple
        }

        frame.setVisible(true)

        //UI Event Listeners
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            void componentResized(ComponentEvent componentEvent) {
                appState.updateSize(panel.size)
            }
        })

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            void mouseMoved(MouseEvent mouseEvent) {
                if (panel.currentDiagram != null && appState.hover) {
                    //Rescale the x and y appropriately, then ask if any rectangle is highlighted
                    double x = mouseEvent.x / (1.0 * panel.width)
                    double y = 1.0 - (mouseEvent.y / (1.0 * panel.height))

                    //We subtract one from currRect because we want to be able to highlight whatever we can "see" meaning see through the topmost rect
                    int highlighted = panel.currentDiagram.getTopRectangleNumberContaining(x, y, appState.currRect - 1)
                    appState.updateHighlighted(highlighted)
                }
            }
        })

        scrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
                appState.updateCurrentRectangle(adjustmentEvent.value)
            }
        })

        playButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent actionEvent) {
                playButton.setEnabled(false)
                pauseButton.setEnabled(true)
                appState.updatePaused(false)
            }
        })

        labelCheckBox.addItemListener(new ItemListener() {
            @Override
            void itemStateChanged(ItemEvent itemEvent) {
                appState.updateShowLabels(itemEvent.stateChange == ItemEvent.SELECTED)
            }
        })

        hoverCheckBox.addItemListener(new ItemListener() {
            @Override
            void itemStateChanged(ItemEvent itemEvent) {
                appState.updateHover(itemEvent.stateChange == ItemEvent.SELECTED)
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

        exportButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent actionEvent) {
                JFileChooser c = new JFileChooser();
                // Demonstrate "Open" dialog:
                int rVal = c.showSaveDialog(frame);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    def file = c.getSelectedFile()
                    file.withWriter {out ->
                        out.write(appState.diagramHistory.last().toString())
                    }
                    JOptionPane.showMessageDialog(frame, "Saved to ${file.getAbsolutePath()}", "Saved", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        })

        //App state Listener
        appState.register(new AppStateListener() {
            @Override
            void updateState(AppState state) {
                if(!scrollBar.enabled || state.maxRect > scrollBar.maximum + 1) {
                    scrollBar.setValues(appState.maxRect, 1, 1, appState.maxRect + 1)
                    scrollBar.enabled = true
                }
                if (state.completed) {
                    frame.setTitle(state.title + " - DONE!")
                    pauseButton.setEnabled(false)
                    playButton.setEnabled(false)
                } else {
                    frame.setTitle(state.title)
                }
            }
        })
    }
}

