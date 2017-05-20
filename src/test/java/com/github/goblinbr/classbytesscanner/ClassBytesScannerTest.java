package com.github.goblinbr.classbytesscanner;

import com.another.pack.one.*;
import com.another.pack.two.ClassTwo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class ClassBytesScannerTest {

    private ClassBytesScanner classBytesScanner = new ClassBytesScanner();

    @Test
    public void scanClassNull() throws Exception {
        Set<Class> classes = classBytesScanner.scanClass(null);
        Assert.assertEquals("Should return empty set", 0, classes.size());
    }

    @Test
    public void scanClassThatDoesntReferenceOthers() throws Exception {
        Set<Class> classes = classBytesScanner.scanClass(SadClass.class);
        Assert.assertEquals("Should return only one class", 1, classes.size());
        Assert.assertEquals("Should return Object", Object.class, classes.iterator().next());
    }

    @Test
    public void scanClassIgnoringJavaLang() throws Exception {
        Set<Class> classes = classBytesScanner.scanClass(SadClass.class, "UTF-8", false, new IgnoreClassIf() {
            public boolean ignoreIf(Class clazz) {
                return clazz.getName().startsWith("java.lang");
            }
        });
        Assert.assertEquals("Should return empty set", 0, classes.size());
    }

    @Test
    public void scanClassRecursivelyIgnoringJavaSunAndSimpleJson() throws Exception {
        Set<Class> classes = classBytesScanner.scanClass(ClassOne.class, "UTF-8", true, new IgnoreClassIf() {
            public boolean ignoreIf(Class clazz) {
                String name = clazz.getName();
                return name.startsWith("java.") || name.startsWith("sun.") || name.startsWith("org.json.simple");
            }
        });
        Assert.assertEquals("Should return 6 classes", 6, classes.size());
        Class[] shouldContainClasses = {AbstractClass.class, EnumOne.class, InterfaceOne.class, AnnotationOne.class, ClassTwo.class, ClassOne.class};
        for(Class c : shouldContainClasses) {
            Assert.assertTrue("Should contain " + c.getName(), classes.contains(c));
        }
    }

    @Test
    public void scanClassRecursivelyIgnoringJavaSunAnotherPack() throws Exception {
        Set<Class> classes = classBytesScanner.scanClass(ClassTwo.class, "UTF-8", true, new IgnoreClassIf() {
            public boolean ignoreIf(Class clazz) {
                String name = clazz.getName();
                return name.startsWith("java.") || name.startsWith("sun.") || name.startsWith("com.another.pack");
            }
        });
        for(Class c : classes) {
            Assert.assertTrue("Should not contain " + c.getName(), c.getName().startsWith("org.json.simple"));
        }
    }
}
