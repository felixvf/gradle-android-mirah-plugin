package de.ferey.android.libproject.app;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import de.ferey.android.libproject.R;
import de.ferey.android.libproject.lib1.Lib1MirahActivity;

public class Lib1MirahActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1MirahActivity> {
    public Lib1MirahActivityJavaTest() {
        super(Lib1MirahActivity.class);
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        assertEquals("Lib1Java", ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }
}
