package com.another.pack.two;

import com.another.pack.one.ClassOne;
import org.json.simple.parser.JSONParser;

public class ClassTwo {

    public void doSomething() {
        ClassOne classOne = new ClassOne();
        classOne.doAnotherThing();

        JSONParser parser = new JSONParser();
        parser.reset();
    }
}
