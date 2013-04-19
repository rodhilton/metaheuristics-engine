package com.rodhilton.metaheuristics.rectanglevisibility.gui

import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram

import java.awt.Dimension

public class AppState {
//    VisibilityDiagram diagram=null
    List<VisibilityDiagram> diagramHistory=new ArrayList<VisibilityDiagram>()
    Dimension size=new Dimension(0,0)
    boolean completed=false
    String title=""
    int width=0
    int height=0
    int currRect=0;
    int maxRect=0;
    boolean paused;

    List<AppStateListener> listeners = new ArrayList<AppStateListener>()

    synchronized void updatePaused(boolean paused) {
        this.paused = paused
        notifyListeners()
    }

    synchronized void updateCurrentRectangle(int rectNum) {
        currRect = rectNum
        notifyListeners()
    }

    synchronized void updateTitle(String title) {
        this.title=title
        notifyListeners()
    }

    synchronized void updateDiagram(VisibilityDiagram diagram) {
        diagramHistory.add(diagram)
        notifyListeners()
    }

    synchronized void updateSize(Dimension newSize) {
        this.size = newSize
        this.width=(int)newSize.width
        this.height=(int)newSize.height
        notifyListeners()
    }

    synchronized void updateCompleted() {
        this.completed = true
        notifyListeners()
    }

    void unregister(AppStateListener listener) {
        listeners.remove(listener)
    }

    void register(AppStateListener listener) {
        listeners.add(listener)
    }

    private void notifyListeners() {
        for (AppStateListener listener : listeners) {
            listener.updateState(this)
        }
    }
}
