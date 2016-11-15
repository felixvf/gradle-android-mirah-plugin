package de.ferey.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.view.ViewGroup
import android.widget.TextView
import junit.framework.Assert
import java.util.TreeMap

class Lib1JavaActivityTest < ActivityInstrumentationTestCase2
  
  def initialize
    super(Lib1JavaActivity.class)
  end
  
  def test1:void
    Assert.assertTrue(true)
  end

  def test2:void
    Assert.assertEquals("Lib1Java", getActivity.findViewById(android::R.id.content).as!(ViewGroup).getChildAt(0).as!(TextView).getText)
  end

  def test3:void
    map = TreeMap.new
    map.put("1", "Lib1Java")
    map.put("2", Lib1Java.new.getName)
    Assert.assertEquals(map["1"], map["2"])
  end
end
