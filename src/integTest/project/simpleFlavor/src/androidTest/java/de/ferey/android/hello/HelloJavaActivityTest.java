package de.ferey.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import mirah.collection.concurrent.TrieMap;

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
        assertEquals(flavor + "Java" + flavor + "Mirah", ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }

    public void testCallMirahLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", flavor + "Java" + flavor + "Mirah");
        assertEquals(map.apply("x"), ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }
}
