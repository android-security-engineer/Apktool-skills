package brut.androlib.search;

import brut.androlib.Config;
import brut.androlib.output.JsonOutput;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ApkSearcherTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";
    private final Config config = new Config("test");

    @Test
    public void testSearchClassesReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, config);
        SearchResult result = searcher.searchClasses(".*");
        assertNotNull(result);
        assertEquals("classes", result.getType());
    }

    @Test
    public void testSearchClassesWithPattern() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, config);
        SearchResult result = searcher.searchClasses("Activity");
        assertNotNull(result);
        assertEquals("classes", result.getType());
    }

    @Test
    public void testSearchStringsReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, config);
        SearchResult result = searcher.searchStrings(".*");
        assertNotNull(result);
        assertEquals("strings", result.getType());
    }

    @Test
    public void testSearchMethodsWithPattern() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, config);
        SearchResult result = searcher.searchMethods("onCreate");
        assertNotNull(result);
        assertEquals("methods", result.getType());
    }

    @Test
    public void testSearchResultJsonOutput() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, config);
        SearchResult result = searcher.searchClasses("Activity");
        String json = JsonOutput.toJson(result);
        assertNotNull(json);
        assertTrue("JSON should contain query field", json.contains("query"));
        assertTrue("JSON should contain matches field", json.contains("matches"));
    }
}
