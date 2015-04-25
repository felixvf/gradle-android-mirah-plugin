package de.ferey.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import de.ferey.android.libproject.R
import junit.framework.Assert
import de.ferey.android.libproject.lib1.Lib1ScalaActivity

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity](classOf[Lib1ScalaActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }
}
