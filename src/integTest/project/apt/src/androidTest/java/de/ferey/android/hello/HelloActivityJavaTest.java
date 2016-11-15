package de.ferey.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import java.util.TreeMap;

public class HelloActivityJavaTest extends ActivityInstrumentationTestCase2<HelloActivity_> {
    @SuppressWarnings("deprecation")
    public HelloActivityJavaTest() {
        super("de.ferey.android.hello", HelloActivity_.class);
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        assertEquals(new HelloJava().say() /*+ "\n" + new HelloMirah().say()*/, ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText()); // Joint compilation is currently not supported
    }

    public void testCallMirahLibraryClassOfNotUsedByMainApp() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("x", new HelloJava().say() /*+ "\n" + new HelloMirah().say()*/); // Joint compilation is currently not supported
        assertEquals(map.get("x"), ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }
}
