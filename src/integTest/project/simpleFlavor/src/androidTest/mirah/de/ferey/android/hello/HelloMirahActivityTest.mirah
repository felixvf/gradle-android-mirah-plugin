package de.ferey.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import mirah.collection.concurrent.TrieMap

class HelloMirahActivityTest extends ActivityInstrumentationTestCase2[HelloMirahActivity]("de.ferey.android.hello", classOf[HelloMirahActivity]) {
  var flavor: String = _

  override def setUp() {
    flavor = getInstrumentation().getTargetContext().getPackageName().replaceFirst(".*\\.", "")
  }

  def testSimpleAssertion() {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion() {
    Assert.assertEquals(f"${flavor}Java${flavor}Mirah", getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }

  def testCallMirahLibraryClassOfNotUsedByMainApp() {
    val map = new TrieMap[String, String]
    map.put("x", f"${flavor}Java${flavor}Mirah")
    Assert.assertEquals(map("x"), getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }
}
