package de.ferey.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import java.util.TreeMap
import junit.framework.Assert

class Lib1MirahActivityTest < ActivityInstrumentationTestCase2

  def initialize
    super("de.ferey.android.libproject.lib1",Lib1MirahActivity.class)
  end
  
  def testSimpleAssertion:void
    Assert.assertTrue(true)
  end

  def testSimpleActivityAssertion:void
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end

  def testCallMirahLibraryClassOfNotUsedByMainApp:void
    map = TreeMap.new
    map.put("x", "Lib1Java")
    Assert.assertEquals(map["x"], getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end
end
