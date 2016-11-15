package de.ferey.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import java.util.TreeMap;

public class HelloJavaActivityTest extends ActivityInstrumentationTestCase2<HelloJavaActivity> {
    String flavor;

    @SuppressWarnings("deprecation")
    public HelloJavaActivityTest() {
        super("de.ferey.android.hello", HelloJavaActivity.class);
    }

    @Override
    public void setUp() {
        flavor = getInstrumentation().getTargetContext().getPackageName().replaceFirst(".*\\.", "");
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        assertEquals(flavor + "Java" /* + flavor + "Mirah" */, ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText()); // Joint compilation is currently not supported
    }

    public void testCallMirahLibraryClassOfNotUsedByMainApp() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("x", flavor + "Java" /* + flavor + "Mirah" */); // Joint compilation is currently not supported
        assertEquals(map.get("x"), ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }
}
