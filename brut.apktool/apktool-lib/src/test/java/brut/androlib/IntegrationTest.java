package brut.androlib;

import brut.androlib.analyze.*;
import brut.androlib.ai.AiPromptBuilder;
import brut.androlib.output.JsonOutput;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class IntegrationTest extends BaseTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";
    private File apkFile;

    @Before
    public void setUp() throws Exception {
        super.beforeEachTest();
        apkFile = new File(getClass().getResource(TEST_APK).getFile());
    }

    @Test
    public void testFullAnalysisPipeline() throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ApkSummary summary = analyzer.getSummary();
        assertNotNull("Summary should not be null", summary);
        assertTrue("File size should be positive", summary.getFileSize() > 0);

        ManifestInfo manifest = analyzer.getManifestInfo();
        assertNotNull("Manifest should not be null", manifest);

        SecurityReport report = analyzer.getSecurityReport();
        assertNotNull("Security report should not be null", report);
        assertTrue("Risk score 0-100", report.getRiskScore() >= 0 && report.getRiskScore() <= 100);

        String summaryJson = JsonOutput.toJson(summary);
        String manifestJson = JsonOutput.toJson(manifest);
        String reportJson = JsonOutput.toJson(report);
        assertTrue("Summary JSON should start with {", summaryJson.trim().startsWith("{"));
        assertTrue("Manifest JSON should start with {", manifestJson.trim().startsWith("{"));
        assertTrue("Report JSON should start with {", reportJson.trim().startsWith("{"));
    }

    @Test
    public void testSearchPipeline() throws Exception {
        ApkSearcher searcher = new ApkSearcher(apkFile, sConfig);

        SearchResult classResult = searcher.searchClasses("Activity");
        assertNotNull(classResult);
        assertEquals("classes", classResult.getType());

        String json = JsonOutput.toJson(classResult);
        assertTrue("JSON should contain matches", json.contains("matches"));
    }

    @Test
    public void testAiPipeline() throws Exception {
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, sConfig);

        String explainPrompt = builder.buildExplainPrompt();
        assertNotNull(explainPrompt);
        assertTrue("Explain prompt should mention permissions", explainPrompt.contains("Permissions"));

        String securityPrompt = builder.buildSecurityReviewPrompt();
        assertNotNull(securityPrompt);
        assertTrue("Security prompt should mention security", securityPrompt.contains("security"));

        String summarizePrompt = builder.buildSummarizePrompt();
        assertNotNull(summarizePrompt);
        assertTrue("Summarize prompt should request summary", summarizePrompt.contains("summary"));
    }

    @Test
    public void testResourceSummaryPipeline() throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ResourceSummary summary = analyzer.getResourceSummary();
        assertNotNull(summary);

        String json = JsonOutput.toJson(summary);
        assertTrue("JSON should contain typeCounts", json.contains("typeCounts"));
    }
}
