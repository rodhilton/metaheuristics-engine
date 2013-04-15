package com.rodhilton.metaheuristics.collections;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class ScoredSet<T> {

    Map<BigDecimal, List<T>> map;
    SortedSet<BigDecimal> scores;
    int size;

    public ScoredSet() {
        this.map = new HashMap<BigDecimal, List<T>>();
        this.scores = new TreeSet<BigDecimal>(new Comparator<BigDecimal>() {
            @Override
            public int compare(BigDecimal t, BigDecimal t1) {
                //Store in reverse order
                return t1.compareTo(t);
            }
        });
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
        if (!map.containsKey(score) ) {
            map.put(score, new ArrayList<T>());
            scores.add(score);
        }
        List<T> list = map.get(score);
        list.add(t);
        size++;
    }

    public int size() {
        return size;
    }

    public T getWorst() {
        BigDecimal bestScore = scores.last();
        List<T> list = map.get(bestScore);
        return list.get(0);
    }

    public T getBest() {
        return getTop(1).get(0);
    }

    public List<T> getTop(int count) {
        if(count>size) throw new ArrayIndexOutOfBoundsException("Requested "+count+", only "+size+" available");

        ArrayList<T> result = new ArrayList<T>();
        Iterator<BigDecimal> scoreIterator = scores.iterator();

        while(result.size()<count) {
            int howManyLeft = count - result.size();
            BigDecimal score = scoreIterator.next();

            List<T> listforScore = map.get(score);
            List<T> subList = listforScore.subList(0, Math.min(listforScore.size(), howManyLeft));
            result.addAll(subList);
        }
        return result;
    }

    public List<T> sortedElements() {
        List<T> newList = new ArrayList<T>();
        for(BigDecimal score: scores) {
                newList.addAll(map.get(score));
        }
        return newList;
    }

}