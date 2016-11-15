package de.ferey.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert

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
end
