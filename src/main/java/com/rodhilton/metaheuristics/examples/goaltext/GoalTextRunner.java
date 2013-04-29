package com.rodhilton.metaheuristics.examples.goaltext;

import com.google.common.base.Supplier;
import com.rodhilton.metaheuristics.algorithms.MetaheuristicAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;
import com.rodhilton.metaheuristics.simulator.Simulator;
import com.rodhilton.metaheuristics.simulator.SimulatorCallback;

import java.util.Collections;
import java.util.List;

public class GoalTextRunner {
    public static void main(String[] args) {
        final String goalText="tobeornottobe";

        Simulator simulator = new Simulator(new Supplier<MetaheuristicAlgorithm>() {

            @Override
            public MetaheuristicAlgorithm get() {
                return new GoalText(goalText);
            }
        });

        SimulatorCallback<MetaheuristicAlgorithm> callback = new SimulatorCallback<MetaheuristicAlgorithm>() {

            @Override
            public void call(ScoredSet<MetaheuristicAlgorithm> everything) {
                System.out.println("-----");
                List<MetaheuristicAlgorithm> top = everything.getTop(10);
                Collections.shuffle(top);
                for(MetaheuristicAlgorithm alg: top) {
                    GoalText goalText = (GoalText)alg;
                    System.out.println(" "+goalText.toString());
                }
            }
        };

        simulator.registerCallback(callback);
        simulator.startSimulation();
    }

}
