package de.ferey.android.libproject.lib1

import junit.framework.{TestCase, Assert}
import mirahz.Mirahz._

class Lib1JavaTest extends TestCase {
  def testCallMirahClass() {
    Assert.assertEquals("Lib1Mirah", new Lib1Mirah().getName)
  }

  def testMirahzUsability() {
    Assert.assertEquals(12345, "12345".parseInt.getOrElse(0))
  }
}
