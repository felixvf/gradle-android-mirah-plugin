package de.ferey.android.libproject.app

import junit.framework.TestCase
import junit.framework.Assert
import de.ferey.android.libproject.lib1.Lib1Mirah

class Lib1JavaTest < TestCase
  def test1:void
    Assert.assertEquals("Lib1Mirah", Lib1Mirah.new.getName)
  end
end
