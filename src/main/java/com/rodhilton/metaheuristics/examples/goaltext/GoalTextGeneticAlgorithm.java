package com.rodhilton.metaheuristics.examples.goaltext;

import com.rodhilton.metaheuristics.algorithms.GeneticAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GoalTextGeneticAlgorithm implements GeneticAlgorithm<String> {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private String goalText;

    public GoalTextGeneticAlgorithm(String goalText) {
        this.goalText = goalText.toLowerCase();
    }

    @Override
    public String mutate(String child) {
        byte[] bytes = child.getBytes();
        int replaceIndex = new Random().nextInt(child.length());
        bytes[replaceIndex] = (byte) ALPHABET.charAt(new Random().nextInt(ALPHABET.length()));
        return new String(bytes);
    }

    @Override
    public List<String> crossover(List<String> parents) {
        String parent1 = parents.get(0);
        String parent2 = parents.get(1);

        System.out.println("   Parent 1: " + parent1);
        System.out.println("   Parent 2: " + parent2);

        int pivot = new Random().nextInt(goalText.length() - 7) + 4;

        System.out.println("   Pivot " + pivot);

        String parent1Left = parent1.substring(0, pivot);
        String parent2Left = parent2.substring(0, pivot);
        String parent1Right = parent1.substring(pivot, goalText.length());
        String parent2Right = parent2.substring(pivot, goalText.length());

        String child1 = parent1Left + parent2Right;
        String child2 = parent2Left + parent1Right;

        List<String> things = new ArrayList<String>();
        things.add(child1);
        things.add(child2);
        return things;
    }

    @Override
    public String initialize() {
        String myString = "";
        for (int i = 0; i < goalText.length(); i++) {
            char randomChar = ALPHABET.charAt(new Random().nextInt(ALPHABET.length()));
            myString = myString + randomChar;
        }
        return myString;
    }

    @Override
    public Number fitness(String candidate) {
        int score = 0;
        for (int i = 0; i < candidate.length(); i++) {
            if (goalText.charAt(i) == candidate.charAt(i)) {
                score++;
            }
        }
        return score;
    }
}
