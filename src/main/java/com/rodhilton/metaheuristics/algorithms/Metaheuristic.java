package com.rodhilton.metaheuristics.algorithms;

import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.List;

public interface Metaheuristic<T>{
    T initialize();
    Number score(T candidate);
    List<T> combine(ScoredSet<T> scoredGeneration);

}
