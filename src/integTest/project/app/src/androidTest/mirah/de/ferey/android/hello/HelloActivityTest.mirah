package de.ferey.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert

import java.util.TreeMap

class HelloActivityTest < ActivityInstrumentationTestCase2
    
  def initialize
    super("de.ferey.android.hello",HelloActivity.class)
  end
  
  def testSimpleAssertion:void
    Assert.assertTrue(true)
  end

  def testSimpleActivityAssertion:void
    Assert.assertEquals(HelloJava.new.say + "\n" + HelloMirah.new.say, HelloActivity(getActivity).findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end

  def testCallMirahLibraryClassOfNotUsedByMainApp:void
    map = TreeMap.new
    map.put("x", HelloJava.new.say + "\n" + HelloMirah.new.say)
    Assert.assertEquals(map["x"], HelloActivity(getActivity).findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end
end
