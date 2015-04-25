package de.ferey.android.libproject.lib1

import junit.framework.{TestCase, Assert}

class Lib1JavaTest extends TestCase {
  def test1() {
    Assert.assertEquals("Lib1Mirah", new Lib1Mirah().getName)
  }
}
