package de.ferey.android.hello

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class HelloActivity < Activity
  $Override def onCreate(savedInstanceState:Bundle)
    super
    setContentView(R.layout.activity_hello)
    mirahTextView = findViewById(R.id.mirah_text_view).as!(TextView)
    mirahTextView.setText(HelloJava.new.say)

    Log.d("debug", "boo")
  end
end
