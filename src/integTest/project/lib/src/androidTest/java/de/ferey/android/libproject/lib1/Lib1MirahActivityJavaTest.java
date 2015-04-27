package de.ferey.android.libproject.lib1;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;


import mirah.collection.concurrent.TrieMap;

public class Lib1MirahActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1MirahActivity> {
    public Lib1MirahActivityJavaTest() {
        super("de.ferey.android.libproject.lib1", Lib1MirahActivity.class);
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        assertEquals("Lib1Java", ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }

    public void testCallMirahLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", "Lib1Java");
        assertEquals(map.apply("x"), ((TextView) getActivity().findViewById(R.id.mirah_text_view)).getText());
    }
}