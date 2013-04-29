package com.rodhilton.metaheuristics.examples.goaltext;

import com.rodhilton.metaheuristics.algorithms.MetaheuristicAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GoalText implements MetaheuristicAlgorithm<GoalText>{
    private String goal;
    private String myString;
    private static final String ALPHABET="abcdefghijklmnopqrstuvwxyz";

    public GoalText(String goal) {
        this.goal = goal.toLowerCase();
        this.myString = "";
        for(int i=0;i<goal.length();i++) {
            char randomChar = ALPHABET.charAt(new Random().nextInt(ALPHABET.length()));
            this.myString = this.myString + randomChar;
        }
    }

    public GoalText(String goal, String myString) {
        this.goal = goal;
        this.myString = myString;
    }

    @Override
    public Number fitness() {
        int score =0;
        for(int i=0;i<goal.length();i++) {
            if(goal.charAt(i) == myString.charAt(i)) {
                score++;
            }
        }
        return score;
    }

    @Override
    public List<GoalText> combine(ScoredSet<GoalText> scoredGeneration) {
        List<GoalText> bestTwo = scoredGeneration.getTop(2);
        GoalText parent1 = bestTwo.get(0);
        GoalText parent2 = bestTwo.get(1);

        System.out.println("   Parent 1: "+parent1);
        System.out.println("   Parent 2: "+parent2);

        int pivot = new Random().nextInt(goal.length()-7)+4;

        System.out.println("   Pivot "+pivot);

        String parent1Left = parent1.myString.substring(0,pivot);
        String parent2Left = parent2.myString.substring(0,pivot);
        String parent1Right = parent1.myString.substring(pivot,goal.length());
        String parent2Right = parent2.myString.substring(pivot,goal.length());

        String child1 = parent1Left + parent2Right;
        String child2 = parent2Left + parent1Right;

        System.out.println("   Child 1: "+child1);
        System.out.println("   Child 2: "+child2);

        List<GoalText> newGeneration = new ArrayList<GoalText>();
        for(int i=0;i<scoredGeneration.size()/2;i++) {
            newGeneration.add(mutate(child1));
            newGeneration.add(mutate(child2));
        }

        return newGeneration;
    }

    private GoalText mutate(String child) {
        byte[] bytes = child.getBytes();
        int replaceIndex = new Random().nextInt(child.length());
        bytes[replaceIndex] = (byte)ALPHABET.charAt(new Random().nextInt(ALPHABET.length()));
        return new GoalText(goal, new String(bytes));
    }

    public String toString() {
        return "'"+myString+"' = "+fitness();
    }
}
