package de.ferey.android.libproject.lib1

import junit.framework.TestCase
import junit.framework.Assert

class Lib1JavaTest < TestCase
  def testCallMirahClass:void
    Assert.assertEquals("Lib1Mirah", Lib1Mirah.new.getName)
  end
end
