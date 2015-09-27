package com.rodhilton.metaheuristics.algorithms;

import com.rodhilton.metaheuristics.collections.ScoredElement;
import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.*;

public interface EvolutionaryProgrammingAlgorithm<T> extends EvolutionaryAlgorithm<T>{

    int boutSize = 10;
    int populationSize = 100;

    @Override
    default List<T> combine(ScoredSet<T> scoredGeneration) {
        //scoredGeneration is the union
        Random random = new Random();

        Map<T, Integer> wins = new HashMap<T, Integer>();

        List<ScoredElement<T>> elementsWithScores = scoredGeneration.getElementsWithScores();

        for(ScoredElement<T> scoredElement: elementsWithScores) {
            for(int i=0;i<boutSize;i++) {
                ScoredElement<T> otherElement = elementsWithScores.get(random.nextInt(elementsWithScores.size()));
                if(scoredElement.getScore().doubleValue() > otherElement.getScore().doubleValue()) {
                    wins.put(scoredElement.getElement(), wins.getOrDefault(scoredElement.getElement(), 0)+1);
                }
            }
        }

        List<T> elements = new ArrayList<T>(wins.keySet());

        Collections.sort(elements, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return wins.get(o2).compareTo(wins.get(o1));
            }
        });

        List<T> parents = elements.subList(0, (int)Math.ceil(scoredGeneration.size()/2.0));

        List<T> children = new ArrayList<T>();

        for(T parent: parents) {
            children.add(this.mutate(parent));
        }


        assert(parents.size() == children.size());

        List<T> population = new ArrayList<T>();
        population.addAll(parents);
        population.addAll(children);

        return population.subList(0, scoredGeneration.size());
    }

    T mutate(T child);
}
