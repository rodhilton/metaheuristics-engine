package com.rodhilton.metaheuristics.examples.goaltext;

import com.google.common.base.Supplier;
import com.rodhilton.metaheuristics.algorithms.EvolutionaryAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;
import com.rodhilton.metaheuristics.simulator.Simulator;
import com.rodhilton.metaheuristics.simulator.SimulatorCallback;

import java.util.Collections;
import java.util.List;

public class GoalTextRunner {
    public static void main(String[] args) {
        final String goalText="tobeornottobe";

        EvolutionaryAlgorithm<GoalText> algo = new GoalText.GoalTextGeneticAlgorithm(goalText);

        Simulator<GoalText> simulator = new Simulator<GoalText>(algo);

        SimulatorCallback<GoalText> callback = new SimulatorCallback<GoalText>() {

            @Override
            public void call(ScoredSet<GoalText> everything) {
                System.out.println("-----");
                List<GoalText> top = everything.getTop(10);
                Collections.shuffle(top);
                for(GoalText goalText: top) {
                    System.out.println(" "+goalText.toString());
                }
            }
        };

        simulator.registerCallback(callback);
        simulator.startSimulation();
    }

}
