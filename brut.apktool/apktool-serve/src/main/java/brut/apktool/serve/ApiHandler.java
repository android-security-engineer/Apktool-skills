package brut.apktool.serve;

import brut.androlib.Config;
import brut.androlib.analyze.*;
import brut.androlib.output.JsonOutput;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;

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

    public String handleSecurity(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSecurityReport());
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
}
