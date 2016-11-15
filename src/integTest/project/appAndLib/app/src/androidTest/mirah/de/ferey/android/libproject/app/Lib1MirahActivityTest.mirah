package de.ferey.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import de.ferey.android.libproject.R
import junit.framework.Assert
import de.ferey.android.libproject.lib1.Lib1Java
import de.ferey.android.libproject.lib1.Lib1MirahActivity
import java.util.TreeMap

class Lib1MirahActivityTest < ActivityInstrumentationTestCase2
  
  def initialize
    super(Lib1MirahActivity.class)
  end
  
  def test1:void
    Assert.assertTrue(true)
  end

  def test2:void
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end

  def test3:void
    map = TreeMap.new
    map.put("1", "Lib1Java")
    map.put("2", Lib1Java.new.getName)
    Assert.assertEquals(map["1"], map["2"])
  end
end
