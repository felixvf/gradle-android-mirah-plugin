package de.ferey.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert

class Lib1MirahActivityTest extends ActivityInstrumentationTestCase2[Lib1MirahActivity](classOf[Lib1MirahActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.mirah_text_view).asInstanceOf[TextView].getText)
  }
}
