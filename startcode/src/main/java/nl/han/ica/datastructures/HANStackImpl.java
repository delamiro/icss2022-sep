package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

public class HANStackImpl implements IHANStack {
    List<Object> stack = new ArrayList<>();

    @Override
    public void push(Object value) {
        stack.add(value);
    }

    @Override
    public Object pop() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        Object lastValue = stack.get(stack.size()-1);
        stack.remove(stack.size()-1);
        return lastValue;
    }

    @Override
    public Object peek() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        Object lastValue = stack.get(stack.size()-1);
        return lastValue;
    }
}
