package de.ferey.android.libproject.lib1;

import junit.framework.TestCase;

public class Lib1ScalaTest extends TestCase {
    public void test1() {
        assertEquals("Lib1Java", new Lib1Java().getName());
    }
}
