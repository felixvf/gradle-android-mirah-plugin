package de.ferey.android.libproject.app;

import junit.framework.TestCase;

import de.ferey.android.libproject.lib1.Lib1Mirah;

public class Lib1MirahTest extends TestCase {
    public void test1() {
        assertEquals("Lib1Mirah", new Lib1Mirah().getName());
    }
}
