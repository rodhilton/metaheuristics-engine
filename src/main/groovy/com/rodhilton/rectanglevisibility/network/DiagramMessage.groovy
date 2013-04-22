package com.rodhilton.rectanglevisibility.network

import com.rodhilton.rectanglevisibility.domain.VisibilityDiagram

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
