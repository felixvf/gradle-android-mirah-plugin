package de.ferey.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import java.util.TreeMap

class HelloMirahActivityTest < ActivityInstrumentationTestCase2
  
  def initialize
    super("de.ferey.android.hello", HelloMirahActivity.class)
  end

  def setUp
    @flavor = getInstrumentation.getTargetContext.getPackageName.replaceFirst(".*\\.", "")
  end

  def testSimpleAssertion:void
    Assert.assertTrue(true)
  end

  def testSimpleActivityAssertion:void
    Assert.assertEquals("#{@flavor}Java#{@flavor}Mirah", getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end

  def testCallMirahLibraryClassOfNotUsedByMainApp:void
    map = TreeMap.new
    map.put("x", "#{flavor}Java#{flavor}Mirah")
    Assert.assertEquals(map["x"], getActivity.findViewById(R.id.mirah_text_view).as!(TextView).getText)
  end
end
