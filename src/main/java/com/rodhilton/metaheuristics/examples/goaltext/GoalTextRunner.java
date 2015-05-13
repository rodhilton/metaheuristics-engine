package com.rodhilton.metaheuristics.examples.goaltext;

import com.rodhilton.metaheuristics.algorithms.EvolutionaryAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;
import com.rodhilton.metaheuristics.simulator.Simulator;
import com.rodhilton.metaheuristics.simulator.SimulatorCallback;

import java.util.Collections;
import java.util.List;

public class GoalTextRunner {
    public static void main(String[] args) {
        final String goalText = "tobeornottobe";

        EvolutionaryAlgorithm<String> algo = new GoalTextGeneticAlgorithm(goalText);

        Simulator<String> simulator = new Simulator<>(algo);

        SimulatorCallback<String> callback = (ScoredSet<String> everything) -> {
            System.out.println("-----");
            System.out.println("Iteration: "+simulator.getIterations());
            List<String> top = everything.getTop(10);
            Collections.shuffle(top);
            for (String best : top) {
                System.out.println(" " + best + " (" + algo.fitness(best) + ")");
            }

            if(algo.fitness(everything.getBest()).intValue() == goalText.length()) {
                simulator.stopSimulation();
            }
        };

        simulator.registerCallback(callback);
        simulator.startSimulation();
    }

}
