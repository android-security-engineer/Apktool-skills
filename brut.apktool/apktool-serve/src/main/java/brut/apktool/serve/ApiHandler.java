package brut.apktool.serve;

import brut.androlib.Config;
import brut.androlib.analyze.*;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkBuilder;
import brut.androlib.res.Framework;
import brut.androlib.output.JsonOutput;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;
import brut.androlib.ai.AiPromptBuilder;
import brut.androlib.ai.AiContext;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiHandler {
    private final Config config;

    public ApiHandler(Config config) {
        this.config = config;
    }

    public String handleInfo(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSummary());
    }

    public String handleManifest(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        return JsonOutput.toJson(manifest);
    }

    public String handlePermissions(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        return JsonOutput.toJson(manifest.getPermissions());
    }

    public String handleComponents(String apkPath, String type) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        java.util.List<ComponentInfo> components;
        switch (type) {
            case "activities": components = manifest.getActivities(); break;
            case "services": components = manifest.getServices(); break;
            case "receivers": components = manifest.getReceivers(); break;
            case "providers": components = manifest.getProviders(); break;
            default: components = java.util.Collections.emptyList(); break;
        }
        return JsonOutput.toJson(components);
    }

    public String handleAllComponents(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getAllComponents());
    }

    public String handleSdkInfo(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        Map<String, String> sdkInfo = new LinkedHashMap<>();
        if (manifest.getMinSdkVersion() != null) sdkInfo.put("minSdkVersion", manifest.getMinSdkVersion());
        if (manifest.getTargetSdkVersion() != null) sdkInfo.put("targetSdkVersion", manifest.getTargetSdkVersion());
        if (manifest.getMaxSdkVersion() != null) sdkInfo.put("maxSdkVersion", manifest.getMaxSdkVersion());
        return JsonOutput.toJson(sdkInfo);
    }

    public String handleSecurity(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSecurityReport());
    }

    public String handleApiSurface(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getApiSurface());
    }

    public String handleSigning(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSigningInfo());
    }

    public String handleStructure(String apkPath) throws Exception {
        StructureInfo info = ApkDiff.getStructure(new File(apkPath), config);
        return JsonOutput.toJson(info);
    }

    public String handleAnalyze(String apkPath) throws Exception {
        File apkFile = new File(apkPath);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        AnalyzeResult result = new AnalyzeResult();
        result.setSummary(analyzer.getSummary());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setManifest(analyzer.getManifestInfo());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setSecurity(analyzer.getSecurityReport());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setApiSurface(analyzer.getApiSurface());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setResources(analyzer.getResourceSummary());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setSigning(analyzer.getSigningInfo());
        result.setStructure(ApkDiff.getStructure(apkFile, config));
        return JsonOutput.toJson(result);
    }

    public String handleAi(String apkPath, String action) throws Exception {
        AiPromptBuilder builder = new AiPromptBuilder(new File(apkPath), config);
        String prompt;
        switch (action) {
            case "security-review": prompt = builder.buildSecurityReviewPrompt(); break;
            case "summarize": prompt = builder.buildSummarizePrompt(); break;
            default: prompt = builder.buildExplainPrompt(); break;
        }
        return prompt;
    }

    public String handleSearch(String apkPath, String type, String pattern) throws Exception {
        ApkSearcher searcher = new ApkSearcher(new File(apkPath), config);
        SearchResult result;
        switch (type) {
            case "strings": result = searcher.searchStrings(pattern); break;
            case "methods": result = searcher.searchMethods(pattern); break;
            default: result = searcher.searchClasses(pattern); break;
        }
        return JsonOutput.toJson(result);
    }

    public String handleDiff(String apkPath1, String apkPath2) throws Exception {
        DiffResult result = ApkDiff.diff(new File(apkPath1), new File(apkPath2), config);
        return JsonOutput.toJson(result);
    }

    public String handleResources(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getResourceSummary());
    }

    public String handleStrings(String apkPath, String pattern) throws Exception {
        ApkSearcher searcher = new ApkSearcher(new File(apkPath), config);
        SearchResult result = searcher.searchStrings(pattern);
        return JsonOutput.toJson(result);
    }

    public String handleDexList(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getDexList());
    }

    public String handleLocales(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getLocales());
    }

    public String handleNativeLibs(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getNativeLibs());
    }

    public String handleDexInfo(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getDexInfo());
    }

    public String handleDecode(String apkPath, String outputDir) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            File apkFile = new File(apkPath);
            File outDir = outputDir != null ? new File(outputDir) : new File(apkFile.getName().replace(".apk", ""));
            ApkDecoder decoder = new ApkDecoder(apkFile, config);
            decoder.decode(outDir);
            result.put("status", "ok");
            result.put("outputDir", outDir.getAbsolutePath());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return JsonOutput.toJson(result);
    }

    public String handleBuild(String dirPath, String outputApk) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            File dir = new File(dirPath);
            ApkBuilder builder = new ApkBuilder(dir, config);
            File outApk = outputApk != null ? new File(outputApk) : new File(dir, "dist" + File.separator + dir.getName() + ".apk");
            builder.build(outApk);
            result.put("status", "ok");
            result.put("inputDir", dir.getAbsolutePath());
            result.put("outputApk", outApk.getAbsolutePath());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return JsonOutput.toJson(result);
    }

    public String handleInstallFramework(String apkPath) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Framework framework = new Framework(config);
            framework.install(new File(apkPath));
            result.put("status", "ok");
            result.put("framework", apkPath);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return JsonOutput.toJson(result);
    }

    public String handleCleanFrameworks() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Framework framework = new Framework(config);
            framework.cleanDirectory();
            result.put("status", "ok");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return JsonOutput.toJson(result);
    }

    public String handleListFrameworks() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Framework framework = new Framework(config);
            java.util.List<File> frameworks = framework.listDirectory();
            result.put("frameworks", frameworks);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return JsonOutput.toJson(result);
    }

    public String handlePublicizeResources(String arscPath) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Framework framework = new Framework(config);
            framework.publicizeResources(new File(arscPath));
            result.put("status", "ok");
            result.put("arsc", arscPath);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return JsonOutput.toJson(result);
    }
}