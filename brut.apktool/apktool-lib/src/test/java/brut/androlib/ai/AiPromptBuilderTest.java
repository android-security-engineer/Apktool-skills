package brut.androlib.ai;

import brut.androlib.Config;
import brut.androlib.output.JsonOutput;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class AiPromptBuilderTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";
    private final Config config = new Config("test");

    @Test
    public void testBuildContextReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, config);
        AiContext context = builder.buildContext();
        assertNotNull(context);
        assertNotNull(context.getApkFileName());
    }

    @Test
    public void testBuildExplainPromptContainsKeySections() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, config);
        String prompt = builder.buildExplainPrompt();
        assertNotNull(prompt);
        assertTrue(prompt.contains("Permissions"));
        assertTrue(prompt.contains("Components"));
        assertTrue(prompt.contains("Security"));
    }

    @Test
    public void testBuildSecurityReviewPromptContainsKeySections() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, config);
        String prompt = builder.buildSecurityReviewPrompt();
        assertNotNull(prompt);
        assertTrue(prompt.contains("security review"));
        assertTrue(prompt.contains("vulnerabilities"));
    }

    @Test
    public void testBuildSummarizePromptIsConcise() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, config);
        String prompt = builder.buildSummarizePrompt();
        assertNotNull(prompt);
        assertTrue(prompt.contains("summary"));
    }

    @Test
    public void testContextJsonOutput() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, config);
        AiContext context = builder.buildContext();
        String json = JsonOutput.toJson(context);
        assertNotNull(json);
        assertTrue(json.contains("apkFileName"));
    }
}
