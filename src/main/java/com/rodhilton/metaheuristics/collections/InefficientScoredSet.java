package com.rodhilton.metaheuristics.collections;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class InefficientScoredSet<T> extends ScoredSet<T>{

    private class Struct {
        BigDecimal score;
        T thing;
    }

    List<Struct> stuff;

    public InefficientScoredSet() {
        this.stuff = new ArrayList<Struct>();
    }

    public void add(Number score, T t) {
        if(score instanceof BigDecimal) {
            add((BigDecimal)score, t);
        } else if(score instanceof BigInteger) {
            add(new BigDecimal((BigInteger)score), t);
        } else {
            add(new BigDecimal(score.doubleValue()), t);
        }
    }

    public synchronized void add(BigDecimal score, T t) {
        Struct struct = new Struct();
        struct.score = score;
        struct.thing = t;
        stuff.add(struct);
    }

    public int size() {
        return stuff.size();
    }

    public T getWorst() {
        return null;
    }

    public T getBest() {
        return getTop(1).get(0);
    }

    public List<T> getTop(int count) {
        List<T> toReturn = new ArrayList<T>();
        List<Struct> copy = new ArrayList<Struct>(this.stuff);

        while(toReturn.size() < count) {
            BigDecimal maxVal = new BigDecimal(0);
            int maxIndex = -1;
            for(int i=0;i<copy.size();i++) {
                Struct struct = copy.get(i);
                if(struct.score.compareTo(maxVal) > 0) {
                    maxVal = struct.score;
                    maxIndex = i;
                }
            }

            toReturn.add(copy.get(maxIndex).thing);
            copy.remove(maxIndex);

        }

        return toReturn;

    }

    public List<T> sortedElements() {
        return null;
    }

}