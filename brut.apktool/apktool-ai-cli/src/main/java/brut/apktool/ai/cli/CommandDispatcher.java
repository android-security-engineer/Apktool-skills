package brut.apktool.ai.cli;

import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.ApkDiff;
import brut.androlib.search.ApkSearcher;
import brut.androlib.ai.AiPromptBuilder;

import java.io.File;
import java.util.Map;

public class CommandDispatcher {

    private final SkillContext context;

    public CommandDispatcher(SkillContext context) {
        this.context = context;
    }

    public Object dispatch(String command, Map<String, String> params) throws Exception {
        ApkAnalyzer analyzer = context.getAnalyzer();
        ApkSearcher searcher = context.getSearcher();

        switch (command) {
            // Metadata & info
            case "info":
                return analyzer.getSummary();
            case "manifest":
                return analyzer.getManifestInfo();
            case "permissions":
                return analyzer.getManifestInfo();
            case "components":
                return analyzer.getAllComponents();
            case "sdk-info":
                return analyzer.getManifestInfo();
            case "resources":
                return analyzer.getResourceSummary();
            case "version":
                return analyzer.getSummary();
            case "manifest-xml":
                return analyzer.getManifestXml();
            case "uses-libs":
                return analyzer.getManifestInfo();

            // Security
            case "security":
                return analyzer.getSecurityReport();
            case "api-surface":
                return analyzer.getApiSurface();
            case "manifest-flags":
                return analyzer.getManifestInfo();
            case "signing":
                return analyzer.getSigningInfo();
            case "permission-detail":
                return analyzer.getPermissionDetail();

            // DEX & code
            case "dex-list":
                return analyzer.getDexList();
            case "dex-info":
                return analyzer.getDexInfo();
            case "dex-strings":
                return analyzer.getDexStrings();
            case "class-list":
                return analyzer.getClassList();
            case "class-info":
                return analyzer.getClassDetail(params.getOrDefault("class", ""));
            case "method-search":
                return analyzer.getMethodSearch(params.getOrDefault("pattern", ".*"));
            case "field-search":
                return analyzer.getFieldSearch(params.getOrDefault("pattern", ".*"));
            case "inheritance":
                return analyzer.getInheritanceInfo(params.getOrDefault("class", ""));
            case "structure":
                return ApkDiff.getStructure(context.getApkFile(), context.getConfig());

            // Search
            case "search": {
                String type = params.getOrDefault("type", "strings");
                String pattern = params.getOrDefault("pattern", ".*");
                switch (type) {
                    case "classes":
                        return searcher.searchClasses(pattern);
                    case "methods":
                        return searcher.searchMethods(pattern);
                    default:
                        return searcher.searchStrings(pattern);
                }
            }
            case "strings":
                return searcher.searchStrings(params.getOrDefault("pattern", ".*"));

            // File & resource
            case "file-list":
                return analyzer.getFileList();
            case "file-hash":
                return analyzer.getFileHash();
            case "asset-list":
                return analyzer.getAssetList();
            case "locales":
                return analyzer.getLocales();
            case "native-libs":
                return analyzer.getNativeLibs();
            case "resource-packages":
                return analyzer.getResourcePackages();
            case "lib-frame-packages":
                return analyzer.getLibFramePackageIds();
            case "apk-info":
                return analyzer.getDecodedApkInfo(context.getApkFile());

            // AI prompts
            case "ai-explain":
                return context.getPromptBuilder().buildExplainPrompt();
            case "ai-security-review":
                return context.getPromptBuilder().buildSecurityReviewPrompt();
            case "ai-summarize":
                return context.getPromptBuilder().buildSummarizePrompt();
            case "ai-context":
                return context.getPromptBuilder().buildContext();

            // Comprehensive analysis
            case "analyze": {
                SkillResult full = new SkillResult("analyze");
                full.put("summary", analyzer.getSummary());
                full.put("manifest", analyzer.getManifestInfo());
                full.put("security", analyzer.getSecurityReport());
                full.put("apiSurface", analyzer.getApiSurface());
                full.put("signing", analyzer.getSigningInfo());
                full.put("resources", analyzer.getResourceSummary());
                full.put("dexList", analyzer.getDexList());
                full.put("nativeLibs", analyzer.getNativeLibs());
                return full;
            }

            // Diff
            case "diff": {
                File apk2 = (File) context.getShared("apk2");
                if (apk2 == null) {
                    throw new IllegalArgumentException("diff requires apk2 in sharedData");
                }
                return ApkDiff.diff(context.getApkFile(), apk2, context.getConfig());
            }

            default:
                throw new IllegalArgumentException("Unknown command: " + command);
        }
    }
}
