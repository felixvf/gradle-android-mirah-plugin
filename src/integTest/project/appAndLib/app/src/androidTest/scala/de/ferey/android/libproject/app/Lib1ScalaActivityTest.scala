package de.ferey.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import de.ferey.android.libproject.R
import junit.framework.Assert
import de.ferey.android.libproject.lib1.{Lib1Java, Lib1MirahActivity}
import mirah.collection.concurrent.TrieMap

class Lib1MirahActivityTest extends ActivityInstrumentationTestCase2[Lib1MirahActivity](classOf[Lib1MirahActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }

  def test3() {
    val map = TrieMap[String, String]()
    map.put("1", "Lib1Java")
    map.put("2", new Lib1Java().getName)
    Assert.assertEquals(map("1"), map("2"))
  }
}
