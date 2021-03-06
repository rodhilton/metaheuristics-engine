package com.rodhilton.metaheuristics.algorithms;

import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.List;

public interface EvolutionaryAlgorithm<T> extends Metaheuristic<T> {
    @Override default Number score(T candidate) {
        return fitness(candidate);
    }

    T initialize();
    Number fitness(T candidate);
    List<T> combine(ScoredSet<T> scoredGeneration);
}
