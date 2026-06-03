package brut.apktool.ai.cli;

import brut.androlib.Config;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkBuilder;
import brut.androlib.res.Framework;
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.search.ApkSearcher;
import brut.androlib.ai.AiPromptBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SkillContext {

    private final File apkFile;
    private final Config config;
    private ApkAnalyzer analyzer;
    private ApkSearcher searcher;
    private AiPromptBuilder promptBuilder;
    private ApkDecoder decoder;
    private ApkBuilder builder;
    private Framework framework;
    private final Map<String, Object> sharedData;

    public SkillContext(File apkFile, Config config) {
        this.apkFile = apkFile;
        this.config = config;
        this.sharedData = new HashMap<>();
    }

    public ApkAnalyzer getAnalyzer() {
        if (analyzer == null) {
            analyzer = new ApkAnalyzer(apkFile, config);
        }
        return analyzer;
    }

    public ApkSearcher getSearcher() {
        if (searcher == null) {
            searcher = new ApkSearcher(apkFile, config);
        }
        return searcher;
    }

    public AiPromptBuilder getPromptBuilder() {
        if (promptBuilder == null) {
            promptBuilder = new AiPromptBuilder(apkFile, config);
        }
        return promptBuilder;
    }

    public ApkDecoder getDecoder() {
        if (decoder == null) {
            decoder = new ApkDecoder(apkFile, config);
        }
        return decoder;
    }

    public ApkBuilder getBuilder() {
        if (builder == null) {
            builder = new ApkBuilder(apkFile, config);
        }
        return builder;
    }

    public Framework getFramework() {
        if (framework == null) {
            framework = new Framework(config);
        }
        return framework;
    }

    public File getApkFile() { return apkFile; }
    public Config getConfig() { return config; }

    public void putShared(String key, Object value) {
        sharedData.put(key, value);
    }

    public Object getShared(String key) {
        return sharedData.get(key);
    }
}
