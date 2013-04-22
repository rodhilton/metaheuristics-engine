package com.rodhilton.metaheuristics.rectanglevisibility.gui.network

import com.rodhilton.metaheuristics.rectanglevisibility.VisibilityDiagram

class DiagramMessage implements Serializable {
    static final long serialVersionUID = 42L;

    VisibilityDiagram diagram
    int generationNum
    String name

    @Override
    String toString() {
        "Name: ${name}, #${generationNum}, Fitness: ${diagram.fitness()}"
    }
}
