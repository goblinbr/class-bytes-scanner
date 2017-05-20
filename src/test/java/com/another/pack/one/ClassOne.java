package com.another.pack.one;

import com.another.pack.two.ClassTwo;

public class ClassOne extends AbstractClass {

    private EnumOne enumOne;

    public void doSomething() {
        ClassTwo classTwo = new ClassTwo();
        classTwo.doSomething();
    }

    public void doAnotherThing() {
    }
}
