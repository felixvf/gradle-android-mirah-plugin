package de.ferey.android.libproject.lib1

import junit.framework.TestCase
import junit.framework.Assert

class Lib1JavaTest < TestCase
  def test1:void
    Assert.assertEquals("Lib1Java", Lib1Java.new.getName)
  end
end
