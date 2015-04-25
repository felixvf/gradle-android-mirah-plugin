package de.ferey.android.libproject.lib1

import android.os.Bundle
import android.app.Activity
import android.widget.TextView

class Lib1MirahActivity extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_lib1_mirah)
    val mirahTextView = findViewById(R.id.mirah_text_view).asInstanceOf[TextView]
    mirahTextView.setText(new Lib1Java().getName)
  }
}
