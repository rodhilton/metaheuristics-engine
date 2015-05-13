package com.rodhilton.metaheuristics.algorithms;

import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.ArrayList;
import java.util.List;

public interface GeneticAlgorithm<T> extends EvolutionaryAlgorithm<T> {

    T mutate(T child);
    List<T> crossover(List<T> parents);

    @Override
    default List<T> combine(ScoredSet<T> scoredGeneration) {
        List<T> bestTwo = scoredGeneration.getTop(2);

        List<T> newParents = crossover(bestTwo);

        int mutations = scoredGeneration.size() / 2;

        List<T> newParent1Offspring = new ArrayList<T>();
        List<T> newParent2Offspring = new ArrayList<T>();

        for (int i = 0; i < mutations; i++) {
            newParent1Offspring.add(mutate(newParents.get(0)));
            newParent2Offspring.add(mutate(newParents.get(1)));
        }

        List<T> things = new ArrayList<T>();

        things.addAll(newParent1Offspring);
        things.addAll(newParent2Offspring);
        //TODO: check for option to keep, remove one and add best

        return things;
    }
}
