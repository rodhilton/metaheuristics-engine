package com.rodhilton.metaheuristics.simulator;

import com.google.common.base.Supplier;
import com.rodhilton.metaheuristics.Counter;
import com.rodhilton.metaheuristics.algorithms.MetaheuristicAlgorithm;
import com.rodhilton.metaheuristics.collections.InefficientScoredSet;
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
        int generationSize = 100;

        List<MetaheuristicAlgorithm> generation = new ArrayList();
        for (int i = 0; i < generationSize; i++) {
            MetaheuristicAlgorithm newElement = supplier.get();
            generation.add(newElement);
        }

        while (!stopRequested) {
            ScoredSet<MetaheuristicAlgorithm> scoredGeneration = scoreGeneration(generation);

            for (SimulatorCallback callback : callbacks) {
                callback.call(scoredGeneration);
            }

            MetaheuristicAlgorithm best = scoredGeneration.getBest();
            generation = best.combine(scoredGeneration);
            Counter.generation++;
        }
    }

    @SuppressWarnings("unchecked")
    private ScoredSet<MetaheuristicAlgorithm> scoreGeneration(List<MetaheuristicAlgorithm> generation) {
        ScoredSet<MetaheuristicAlgorithm> scored = new ScoredSet();
        for (MetaheuristicAlgorithm member : generation) {
            scored.add(member.fitness(), member);
        }
        return scored;
    }

    public void stopSimulation() {
        this.stopRequested = true;
    }
}


