package com.rodhilton.metaheuristics.rectanglevisibility.gui

import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram

import javax.swing.JPanel
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

public class ViewPanel extends JPanel implements AppStateListener {
    AppState currentState;
    VisibilityDiagram currentDiagram;
    int currentGeneration

    public ViewPanel(AppState currentState) {
        this.currentState = currentState
        currentState.register(this)
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(currentDiagram!=null && currentState.width>0 && currentState.height > 0) {
            BufferedImage buff=currentDiagram.render(currentState.width, currentState.height, currentState.currRect, currentState.highlightRect)
            Graphics2D g2 = (Graphics2D)buff.getGraphics()

            if(currentState.showLabels) {
                g2.setColor(Color.BLACK)
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int labelPadding = 5;
                FontMetrics fm = this.getFontMetrics(this.getFont());
                //Label the size, top left
                drawWithOutline("Level: ${currentState.currRect}/${currentDiagram.size}", labelPadding, fm.height, g2)

                //Label the name, top right
                def nameString = "[${currentState.name}]"
                drawWithOutline(nameString, currentState.width-labelPadding-fm.stringWidth(nameString), fm.height, g2);

                //Label the generation, bottom left
                drawWithOutline("Generation: ${currentGeneration}", labelPadding, fm.ascent + currentState.height - fm.height - labelPadding, g2);

                //Label the Fitness, bottom right
                def fitnessString = "Fitness: ${currentDiagram.fitness()}/${currentDiagram.getGoal()}"
                drawWithOutline(fitnessString, currentState.width-labelPadding-fm.stringWidth(fitnessString), fm.ascent + currentState.height - fm.height - labelPadding, g2);

            }

            g.drawImage(buff, 0, 0, null)
        }
    }

    private void drawWithOutline(String string, int x, int y, Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 255))
        g2.drawString(string, x-1, y+1)
        g2.drawString(string, x+1, y+1)
        g2.drawString(string, x-1, y-1)
        g2.drawString(string, x+1, y-1)
        g2.setColor(Color.BLACK)
        g2.drawString(string, x, y)
    }

    void updateState(AppState state) {
        this.currentState = state
        this.currentDiagram = state.hasDiagram() ? state.getDiagram() : null
        this.currentGeneration = state.currentGeneration()
        this.updateUI()
    }

}
