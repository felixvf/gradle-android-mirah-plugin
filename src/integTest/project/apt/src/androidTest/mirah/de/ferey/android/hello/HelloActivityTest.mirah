package de.ferey.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import java.util.TreeMap

class HelloActivityTest < ActivityInstrumentationTestCase2
  
  def initialize
    super("de.ferey.android.hello", HelloActivity_.class)
  end
  
  def testSimpleAssertion:void
    Assert.assertTrue(true)
  end

  def testSimpleActivityAssertion:void
    Assert.assertEquals(HelloJava.new.say /*+ "\n" + HelloMirah.new.say*/, getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText) # Joint compilation is currently not supported
  end

  def testCallMirahLibraryClassOfNotUsedByMainApp:void
    map = TreeMap.new
    map.put("x", HelloJava.new.say /*+ "\n" + HelloMirah.new.say*/) # Joint compilation is currently not supported
    Assert.assertEquals(map["x"], getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end
end
