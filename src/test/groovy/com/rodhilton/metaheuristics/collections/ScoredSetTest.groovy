package com.rodhilton.metaheuristics.collections

import org.junit.Test

class ScoredSetTest {

    @Test
    void shouldGetBest() {
        ScoredSet<String> scoredSet = new ScoredSet<String>()
        scoredSet.add(1.0, "Foo")
        scoredSet.add(0.9, "Baz")
        scoredSet.add(1.0, "Bar")

        assert scoredSet.getBest() == "Foo"
    }

    @Test
    void shouldGetTopN() {
        ScoredSet<String> scoredSet = new ScoredSet<String>()
        scoredSet.add(1.0, "Foo")
        scoredSet.add(0.9, "Baz")
        scoredSet.add(1.0, "Bar")

        assert scoredSet.getTop(2) == ["Foo", "Bar"]
    }

    @Test
    void shouldGetTopAll() {
        ScoredSet<String> scoredSet = new ScoredSet<String>()
        scoredSet.add(1.0, "Foo")
        scoredSet.add(0.9, "Baz")
        scoredSet.add(1.0, "Bar")
        assert scoredSet.size() == 3
        assert scoredSet.getTop(3) == ["Foo", "Bar", "Baz"]
    }

    @Test(expected = ArrayIndexOutOfBoundsException)
    void shouldNotAllowOverRequest() {
        ScoredSet<String> scoredSet = new ScoredSet<String>()
        scoredSet.add(1.0, "Foo")
        scoredSet.add(0.9, "Baz")
        scoredSet.add(1.0, "Bar")
        scoredSet.getTop(4)
    }

    @Test
    void shouldGetTopNComplex() {
        ScoredSet<String> scoredSet = new ScoredSet<String>()
        scoredSet.add(1.0, "Foo")
        scoredSet.add(0.9, "Baz")
        scoredSet.add(1.0, "Bar")
        scoredSet.add(0.2, "Woof")
        scoredSet.add(0.8, "Oink")
        scoredSet.add(0.0, "Moo")
        scoredSet.add(0.8, "Beaver Noise")

        assert scoredSet.getTop(5) == ["Foo", "Bar", "Baz", "Oink", "Beaver Noise"]
    }
}
