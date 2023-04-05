package it.unibo.photocircuit;

import java.util.Objects;

public class Couple<T1, T2> {
    public T1 first;
    public T2 second;

    public Couple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Couple<?, ?> couple = (Couple<?, ?>) o;
        return Objects.equals(first, couple.first) && Objects.equals(second, couple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
