package com.rodhilton.metaheuristics.algorithms;

import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.List;

public interface MetaheuristicAlgorithm<T> {
    public Number fitness();
    List<T> combine(ScoredSet<T> scoredGeneration);
}
