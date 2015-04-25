package de.ferey.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import mirah.collection.concurrent.TrieMap;

public class HelloActivityJavaTest extends ActivityInstrumentationTestCase2<HelloActivity_> {
    @SuppressWarnings("deprecation")
    public HelloActivityJavaTest() {
        super("de.ferey.android.hello", HelloActivity_.class);
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        assertEquals(new HelloJava().say() + "\n" + new HelloMirah().say(), ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }

    public void testCallMirahLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", new HelloJava().say() + "\n" + new HelloMirah().say());
        assertEquals(map.apply("x"), ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }
}
