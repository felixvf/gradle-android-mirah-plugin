package de.ferey.android.hello

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class HelloActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_hello)
    val mirahTextView = findViewById(R.id.mirah_text_view).asInstanceOf[TextView]
    mirahTextView.setText(new HelloJava().say + "\n" + new HelloMirah().say)
  }
}
