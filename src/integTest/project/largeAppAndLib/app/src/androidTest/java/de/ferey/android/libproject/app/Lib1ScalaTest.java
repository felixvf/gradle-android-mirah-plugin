package de.ferey.android.libproject.app;

import junit.framework.TestCase;

import de.ferey.android.libproject.lib1.Lib1Scala;

public class Lib1ScalaTest extends TestCase {
    public void test1() {
        assertEquals("Lib1Scala", new Lib1Scala().getName());
    }
}
