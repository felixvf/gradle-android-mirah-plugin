package de.ferey.android.hello

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class HelloMirahActivity < Activity
  def onCreate(savedInstanceState:Bundle)
    super
    setContentView(R.layout.activity_hello)
    mirahTextView = findViewById(R.id.mirah_text_view).as!(TextView)
    mirahTextView.setText(FlavorJava.new.name + FlavorMirah.new.name)
  end
end
