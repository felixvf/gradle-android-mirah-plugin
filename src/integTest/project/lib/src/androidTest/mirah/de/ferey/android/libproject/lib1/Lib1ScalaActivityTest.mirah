package de.ferey.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import mirah.collection.concurrent.TrieMap
import junit.framework.Assert

class Lib1MirahActivityTest extends ActivityInstrumentationTestCase2[Lib1MirahActivity]("de.ferey.android.libproject.lib1", classOf[Lib1MirahActivity]) {
  def testSimpleAssertion {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }

  def testCallMirahLibraryClassOfNotUsedByMainApp {
    val map = new TrieMap[String, String]
    map.put("x", "Lib1Java")
    Assert.assertEquals(map("x"), getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }
}
