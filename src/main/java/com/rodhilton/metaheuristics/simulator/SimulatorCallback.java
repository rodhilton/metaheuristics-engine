package com.rodhilton.metaheuristics.simulator;

import com.rodhilton.metaheuristics.collections.ScoredSet;

public interface SimulatorCallback<T> {
    void call(ScoredSet<T> everything);
}
