package de.ferey.android.libproject.lib1

import android.os.Bundle
import android.app.Activity
import android.widget.TextView

class Lib1MirahActivity < Activity
  protected def onCreate(savedInstanceState:Bundle)
    super
    setContentView(R.layout.activity_lib1_mirah)
    mirahTextView = findViewById(R.id.mirah_text_view).as!(TextView)
    mirahTextView.setText(Lib1Java.new.getName)
  end
end
