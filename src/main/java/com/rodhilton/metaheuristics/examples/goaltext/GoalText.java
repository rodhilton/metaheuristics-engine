package com.rodhilton.metaheuristics.examples.goaltext;

import com.rodhilton.metaheuristics.algorithms.EvolutionaryAlgorithm;
import com.rodhilton.metaheuristics.algorithms.GeneticAlgorithm;
import com.rodhilton.metaheuristics.collections.ScoredSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GoalText {
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

    public Number fitness() {
        int score =0;
        for(int i=0;i<goal.length();i++) {
            if(goal.charAt(i) == myString.charAt(i)) {
                score++;
            }
        }
        return score;
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

    public static class GoalTextGeneticAlgorithm implements GeneticAlgorithm<GoalText> {

        private String goalText;

        public GoalTextGeneticAlgorithm(String goalText) {
            this.goalText = goalText;
        }

        @Override
        public GoalText mutate(GoalText child) {
            return child.mutate(child.myString);
        }

        @Override
        public List<GoalText> crossover(List<GoalText> parents) {
            GoalText parent1 = parents.get(0);
            GoalText parent2 = parents.get(1);

            System.out.println("   Parent 1: "+parent1);
            System.out.println("   Parent 2: "+parent2);

            int pivot = new Random().nextInt(goalText.length()-7)+4;

            System.out.println("   Pivot "+pivot);

            String parent1Left = parent1.myString.substring(0,pivot);
            String parent2Left = parent2.myString.substring(0,pivot);
            String parent1Right = parent1.myString.substring(pivot, goalText.length());
            String parent2Right = parent2.myString.substring(pivot,goalText.length());

            String child1 = parent1Left + parent2Right;
            String child2 = parent2Left + parent1Right;

            List<GoalText> things = new ArrayList<GoalText>();
            things.add(new GoalText(goalText, child1));
            things.add(new GoalText(goalText, child2));
            return things;
        }

        @Override
        public GoalText initialize() {
            return new GoalText(goalText);
        }

        @Override
        public Number fitness(GoalText candidate) {
            return candidate.fitness();
        }
    }
}
