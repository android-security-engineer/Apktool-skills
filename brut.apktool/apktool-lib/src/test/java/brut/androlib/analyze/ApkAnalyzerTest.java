package brut.androlib.analyze;

import brut.androlib.Config;
import brut.androlib.output.JsonOutput;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ApkAnalyzerTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";
    private final Config config = new Config("test");

    @Test
    public void testGetSummaryReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ApkSummary summary = analyzer.getSummary();
        assertNotNull(summary);
        assertNotNull(summary.getFileName());
        assertTrue(summary.getFileSize() > 0);
    }

    @Test
    public void testGetSummaryDexCount() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ApkSummary summary = analyzer.getSummary();
        assertTrue("Should have at least 1 dex file", summary.getDexCount() >= 1);
    }

    @Test
    public void testGetSummaryHasResources() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ApkSummary summary = analyzer.getSummary();
        assertTrue("Should have resources", summary.isHasResources());
    }

    @Test
    public void testGetManifestInfoReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        assertNotNull(manifest);
    }

    @Test
    public void testGetSecurityReportReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        SecurityReport report = analyzer.getSecurityReport();
        assertNotNull(report);
        assertTrue("Risk score should be 0-100", report.getRiskScore() >= 0 && report.getRiskScore() <= 100);
    }

    @Test
    public void testGetSecurityReportHasFindings() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        SecurityReport report = analyzer.getSecurityReport();
        assertNotNull(report.getFindings());
    }

    @Test
    public void testJsonOutputFromSummary() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ApkSummary summary = analyzer.getSummary();
        String json = JsonOutput.toJson(summary);
        assertNotNull(json);
        assertTrue("JSON should contain fileName", json.contains("fileName"));
        assertTrue("JSON should contain fileSize", json.contains("fileSize"));
    }

    @Test
    public void testJsonOutputFromManifest() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        String json = JsonOutput.toJson(manifest);
        assertNotNull(json);
        assertTrue("JSON should contain permissions", json.contains("permissions"));
    }

    @Test
    public void testJsonOutputFromSecurityReport() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        SecurityReport report = analyzer.getSecurityReport();
        String json = JsonOutput.toJson(report);
        assertNotNull(json);
        assertTrue("JSON should contain riskScore", json.contains("riskScore"));
    }
}
