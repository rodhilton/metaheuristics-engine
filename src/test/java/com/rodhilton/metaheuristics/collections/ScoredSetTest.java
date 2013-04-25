package com.rodhilton.metaheuristics.collections;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ScoredSetTest {

    @Test
    public void shouldGetBest() {
        ScoredSet<String> scoredSet = new ScoredSet<String>();
        scoredSet.add(1.0, "Foo");
        scoredSet.add(0.9, "Baz");
        scoredSet.add(1.0, "Bar");

        assertEquals(scoredSet.getBest(), "Foo");
    }

    @Test
    public void shouldGetTopN() {
        ScoredSet<String> scoredSet = new ScoredSet<String>();
        scoredSet.add(1.0, "Foo");
        scoredSet.add(0.9, "Baz");
        scoredSet.add(1.0, "Bar");

        List<String> top2 = scoredSet.getTop(2);
        assertEquals(top2.size(), 2);
        assertTrue(top2.contains("Foo"));
        assertTrue(top2.contains("Bar"));
    }

    @Test
    public void shouldGetTopAll() {
        ScoredSet<String> scoredSet = new ScoredSet<String>();
        scoredSet.add(1.0, "Foo");
        scoredSet.add(0.9, "Baz");
        scoredSet.add(1.0, "Bar");
        assertEquals(scoredSet.size(), 3);

        List<String> top3 = scoredSet.getTop(3);
        assertEquals(top3.size(), 3);
        assertTrue(top3.contains("Foo"));
        assertTrue(top3.contains("Bar"));
        assertTrue(top3.contains("Baz"));
        assertTrue(top3.indexOf("Bar") < top3.indexOf("Baz"));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shouldNotAllowOverRequest() {
        ScoredSet<String> scoredSet = new ScoredSet<String>();
        scoredSet.add(1.0, "Foo");
        scoredSet.add(0.9, "Baz");
        scoredSet.add(1.0, "Bar");
        scoredSet.getTop(4);
    }

    @Test
    public void shouldGetTopNComplex() {
        ScoredSet<String> scoredSet = new ScoredSet<String>();
        scoredSet.add(1.0, "Foo");
        scoredSet.add(0.9, "Baz");
        scoredSet.add(1.0, "Bar");
        scoredSet.add(0.2, "Woof");
        scoredSet.add(0.8, "Oink");
        scoredSet.add(0.0, "Moo");
        scoredSet.add(0.8, "Beaver Noise");

        List<String> top5 = scoredSet.getTop(5);

        assertEquals(top5.size(), 5);
        assertTrue(top5.contains("Foo"));
        assertTrue(top5.contains("Bar"));
        assertTrue(top5.contains("Baz"));
        assertTrue(top5.contains("Oink"));
        assertTrue(top5.contains("Beaver Noise"));


        assertTrue(top5.indexOf("Foo") < top5.indexOf("Baz"));
        assertTrue(top5.indexOf("Bar") < top5.indexOf("Baz"));
        assertTrue(top5.indexOf("Baz") < top5.indexOf("Oink"));
        assertTrue(top5.indexOf("Baz") < top5.indexOf("Beaver Noise"));
    }


}
