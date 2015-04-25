package de.ferey.android.hello;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelloJavaActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        TextView mirahTextView = (TextView) findViewById(R.id.mirah_text_view);
        mirahTextView.setText(new FlavorJava().name() + new FlavorMirah().name());
    }
}
