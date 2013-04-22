package com.rodhilton.rectanglevisibility.main

import com.rodhilton.rectanglevisibility.domain.VisibilityDiagram

import java.awt.*
import java.util.List

public class AppState {
    //Main State
    private VisibilityDiagram diagram = null //The visibility diagram itself
    private int generationNum = 0 //What generation the simulation is up to
    String name = "" //The name of the user running the simulation
    int maxRect = 0; //The size of the diagram, as in K_{maxRect}

    //Status
    boolean completed = false  //Simulation completed
    boolean paused = false //Simulation paused (likely via UI)

    //UI Specific
    String title = "" //Title of the client, arguable UI specific
    int currRect = 0 //Which one the scrollbar is on, the max level to show
    int width = 0 //The width of the view area
    int height = 0 //The height of the view area
    int highlightRect = -1; //Which rectangle is currently highlighted with mouse (-1 for none)
    boolean showLabels = true //Whether the HUD labels should be rendered
    boolean hover = false //Whether the user has even selected the hoverability

    private List<AppStateListener> listeners = new ArrayList<AppStateListener>()

    synchronized void updateDiagram(VisibilityDiagram diagram, int generationNum, String name = "") {
        this.diagram = diagram
        this.maxRect = diagram.size
        this.name = name
        this.generationNum = generationNum
        notifyListeners()
    }

    synchronized void updateHover(boolean hover) {
        if (!hover) highlightRect = -1
        this.hover = hover;
        notifyListeners()
    }

    synchronized void updateShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        notifyListeners()
    }

    synchronized void updateHighlighted(int highlightRect) {
        this.highlightRect = highlightRect
        notifyListeners()
    }

    synchronized void updatePaused(boolean paused) {
        this.paused = paused
        notifyListeners()
    }

    synchronized void updateCurrentRectangle(int rectNum) {
        currRect = rectNum
        notifyListeners()
    }

    synchronized void updateSize(Dimension newSize) {
//        this.size = newSize
        this.width = (int) newSize.width
        this.height = (int) newSize.height
        notifyListeners()
    }

    synchronized void updateCompleted() {
        this.paused = true
        this.completed = true
        notifyListeners()
    }

//    synchronized void unregister(AppStateListener listener) {
//        listeners.remove(listener)
//    }

    synchronized void register(AppStateListener listener) {
        listeners.add(listener)
    }

    synchronized int currentGeneration() {
        generationNum
    }

    synchronized boolean hasDiagram() {
        diagram != null
    }

    synchronized VisibilityDiagram getDiagram() {
        diagram
    }

    private void notifyListeners() {
        if(hasDiagram()) {
            for (AppStateListener listener : listeners) {
                listener.updateState(this)
            }
        }
    }
}
