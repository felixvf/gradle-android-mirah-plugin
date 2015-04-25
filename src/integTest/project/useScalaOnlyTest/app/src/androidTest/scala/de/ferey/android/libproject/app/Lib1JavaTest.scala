package de.ferey.android.libproject.app

import junit.framework.{TestCase, Assert}
import de.ferey.android.libproject.lib1.Lib1Java

class Lib1JavaTest extends TestCase {
  def test1() {
    Assert.assertEquals("Lib1Java", new Lib1Java().getName)
    Assert.assertEquals(List("Lib1Java").last, Option(new Lib1Java().getName).get)
  }
}
