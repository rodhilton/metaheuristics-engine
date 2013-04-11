package com.rodhilton.metaheuristics.simulator;

import com.google.common.base.Supplier;
import com.rodhilton.metaheuristics.algorithms.MetaheuristicAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.ArrayList;
import java.util.List;

public class Simulator {
    private List<SimulatorCallback> callbacks;
    private boolean stopRequested;
    private Supplier<MetaheuristicAlgorithm> supplier;

    public Simulator(Supplier<MetaheuristicAlgorithm> supplier) {

        this.callbacks = new ArrayList<SimulatorCallback>();
        this.stopRequested = false;
        this.supplier = supplier;
    }

    public void registerCallback(SimulatorCallback callback) {
        callbacks.add(callback);
    }


    @SuppressWarnings("unchecked")
    public void startSimulation() {
        int generationSize = 1000;

        List<MetaheuristicAlgorithm> generation = new ArrayList();
        for (int i = 0; i < generationSize; i++) {
            MetaheuristicAlgorithm newElement = supplier.get();
            generation.add(newElement);
        }

        while (!stopRequested) {
            ScoredSet<MetaheuristicAlgorithm> sortedGeneration = new ScoredSet();
            for (MetaheuristicAlgorithm member : generation) {
                sortedGeneration.add(member.fitness(), member);
            }

            for (SimulatorCallback callback : callbacks) {
                callback.call(sortedGeneration);
            }

            MetaheuristicAlgorithm best = sortedGeneration.getBest();
            generation = best.combine(sortedGeneration);

        }
    }

    public void stopSimulation() {
        this.stopRequested = true;
    }
}


