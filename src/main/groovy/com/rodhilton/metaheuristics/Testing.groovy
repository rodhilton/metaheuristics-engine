package com.rodhilton.metaheuristics

import com.rodhilton.metaheuristics.algorithms.MetaheuristicAlgorithm
import com.rodhilton.metaheuristics.collections.ScoredSet

class Testing implements Serializable, MetaheuristicAlgorithm<Testing> {

    private String sequence
    private String goalSequence


    public Testing(String goalSequence) {
        this.goalSequence = goalSequence
        List<Character> characters = new ArrayList<Character>();
        for(char c:goalSequence.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(goalSequence.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        this.sequence = output.toString()
    }

    public Testing(String goalSequence, String sequence) {
        this.goalSequence = goalSequence
        this.sequence = sequence
    }

    public String toString() {
        return this.sequence
    }

    public Testing mutate() {
        Random random = new Random();
        int index1 = random.nextInt(sequence.length())
        int index2 = random.nextInt(sequence.length())

        StringBuilder sb = new StringBuilder();
        sb.append(sequence);

        char temp = sequence.charAt(index1)
        sb.setCharAt(index1, sequence.charAt(index2));
        sb.setCharAt(index2, temp);

        return new Testing(goalSequence, sb.toString());
    }

    @Override
    public BigInteger fitness() {
        double count=0;
        char[] seqArray = sequence.toCharArray()
        char[] goalArray = goalSequence.toCharArray()
        for(int i=0;i<seqArray.length;i++) {
            char x = seqArray[i]
            char y = goalArray[i]
            if(x==y) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<Testing> combine(ScoredSet<Testing> scoredGeneration) {
        def howMany = scoredGeneration.size()-1
        def newGeneration = (0..howMany).collect {this.mutate()}
        //Include myself
        newGeneration << this
    }

}