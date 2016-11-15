package de.ferey.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import android.view.ViewGroup
import android.widget.TextView
import de.ferey.android.libproject.R
import junit.framework.Assert
import de.ferey.android.libproject.lib1.Lib1Java
import de.ferey.android.libproject.lib1.Lib1JavaActivity

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
end
