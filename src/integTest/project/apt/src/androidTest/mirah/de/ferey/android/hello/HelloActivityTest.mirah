package de.ferey.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import mirah.collection.concurrent.TrieMap

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity_]("de.ferey.android.hello", classOf[HelloActivity_]) {
  def testSimpleAssertion() {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion() {
    Assert.assertEquals(new HelloJava().say + "\n" + new HelloMirah().say, getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }

  def testCallMirahLibraryClassOfNotUsedByMainApp() {
    val map = new TrieMap[String, String]
    map.put("x", new HelloJava().say + "\n" + new HelloMirah().say)
    Assert.assertEquals(map("x"), getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }
}
