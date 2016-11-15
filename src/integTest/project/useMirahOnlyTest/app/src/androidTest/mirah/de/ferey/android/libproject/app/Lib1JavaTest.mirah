package de.ferey.android.libproject.app

import junit.framework.TestCase
import junit.framework.Assert
import de.ferey.android.libproject.lib1.Lib1Java

class Lib1JavaTest < TestCase
  def test1:void
    Assert.assertEquals("Lib1Java", Lib1Java.new.getName)
  end
end
