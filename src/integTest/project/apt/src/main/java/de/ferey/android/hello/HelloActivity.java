package de.ferey.android.hello;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_hello)
public class HelloActivity extends Activity {
    @ViewById
    TextView mirahTextView;

    @AfterViews
    public void fillMirahTextView() {
        mirahTextView.setText(new HelloJava().say() /*+ "\n" + new HelloMirah().say()*/); // Joint compilation is currently not supported
    }
}
