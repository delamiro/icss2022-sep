package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

public class HANStackImpl<T> implements IHANStack<T> {
    private List<T> stack = new ArrayList<>();

    @Override
    public void push(T value) {
        stack.add(value);
    }

    @Override
    public T pop() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        T lastValue = stack.get(stack.size()-1);
        stack.remove(stack.size()-1);
        return lastValue;
    }

    @Override
    public T peek() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        T lastValue = stack.get(stack.size()-1);
        return lastValue;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
