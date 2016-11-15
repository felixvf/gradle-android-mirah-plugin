package de.ferey.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert

class HelloActivityTest < ActivityInstrumentationTestCase2
  
  def initialize
    super(HelloActivity.class)
  end
  
  def test1:void
    Assert.assertTrue(true)
  end

  def test2:void
    Assert.assertEquals("Hello. I'm Java !", getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end
end
