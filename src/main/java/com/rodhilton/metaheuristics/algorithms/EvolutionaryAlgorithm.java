package com.rodhilton.metaheuristics.algorithms;

import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.List;

public interface EvolutionaryAlgorithm<T> {
    T initialize();
    Number fitness(T candidate);
    List<T> combine(ScoredSet<T> scoredGeneration);
}
