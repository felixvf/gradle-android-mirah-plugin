package de.ferey.android.hello

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class HelloActivity < Activity
  def onCreate(savedInstanceState:Bundle)
    super
    setContentView(R.layout.activity_hello)
    mirahTextView = findViewById(R.id.mirah_text_view).as!(TextView)
    mirahTextView.setText(HelloJava.new.say + "\n" + HelloMirah.new.say)
  end
end
