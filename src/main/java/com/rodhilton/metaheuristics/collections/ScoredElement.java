package com.rodhilton.metaheuristics.collections;

public class ScoredElement<T> {
    T element;
    Number score;

    public ScoredElement(T element, Number score) {
        this.element = element;
        this.score = score;
    }

    public Number getScore() {
        return score;
    }

    public T getElement() {
        return element;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoredElement)) return false;

        ScoredElement<?> that = (ScoredElement<?>) o;

        if (!element.equals(that.element)) return false;
        return score.equals(that.score);

    }

    @Override
    public int hashCode() {
        int result = element.hashCode();
        result = 31 * result + score.hashCode();
        return result;
    }


}