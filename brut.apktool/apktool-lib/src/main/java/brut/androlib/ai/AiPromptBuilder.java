package brut.androlib.ai;

import brut.androlib.Config;
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.ComponentInfo;
import brut.androlib.analyze.ManifestInfo;
import brut.androlib.analyze.ResourceSummary;
import brut.androlib.analyze.SecurityReport;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.output.JsonOutput;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiPromptBuilder {
    private final File mApkFile;
    private final Config mConfig;

    public AiPromptBuilder(File apkFile, Config config) {
        mApkFile = apkFile;
        mConfig = config;
    }

    public AiContext buildContext() throws AndrolibException {
        AiContext context = new AiContext();
        ApkAnalyzer analyzer = new ApkAnalyzer(mApkFile, mConfig);

        context.setApkFileName(mApkFile.getName());

        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest != null) {
            context.setPackageName(manifest.getPackageName());
            context.setPermissions(manifest.getPermissions());

            List<String> components = new ArrayList<>();
            for (ComponentInfo c : manifest.getActivities()) components.add("Activity: " + c.getName());
            for (ComponentInfo c : manifest.getServices()) components.add("Service: " + c.getName());
            for (ComponentInfo c : manifest.getReceivers()) components.add("Receiver: " + c.getName());
            for (ComponentInfo c : manifest.getProviders()) components.add("Provider: " + c.getName());
            context.setComponents(components);
        }

        SecurityReport report = analyzer.getSecurityReport();
        context.setSecurityReport(JsonOutput.toJson(report));

        try {
            context.setSigningInfo(JsonOutput.toJson(analyzer.getSigningInfo()));
        } catch (Exception ignored) {}
        try {
            brut.androlib.analyze.ResourceSummary resSummary = analyzer.getResourceSummary();
            context.setResourceSummary(JsonOutput.toJson(resSummary));
        } catch (Exception ignored) {}
        if (manifest != null) {
            Map<String, String> sdkMap = new LinkedHashMap<>();
            if (manifest.getMinSdkVersion() != null) sdkMap.put("minSdkVersion", manifest.getMinSdkVersion());
            if (manifest.getTargetSdkVersion() != null) sdkMap.put("targetSdkVersion", manifest.getTargetSdkVersion());
            context.setSdkInfo(JsonOutput.toJson(sdkMap));
        }

        int totalChars = 0;
        if (context.getManifestXml() != null) totalChars += context.getManifestXml().length();
        totalChars += context.getPermissions().size() * 40;
        totalChars += context.getComponents().size() * 60;
        if (context.getSecurityReport() != null) totalChars += context.getSecurityReport().length();
        if (context.getSigningInfo() != null) totalChars += context.getSigningInfo().length();
        if (context.getSdkInfo() != null) totalChars += context.getSdkInfo().length();
        if (context.getResourceSummary() != null) totalChars += context.getResourceSummary().length();
        context.setEstimatedTokenCount(totalChars / 4);

        return context;
    }

    public String buildExplainPrompt() throws AndrolibException {
        AiContext context = buildContext();
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the following Android application and explain its main functionality:\n\n");
        sb.append("APK File: ").append(context.getApkFileName()).append("\n");
        sb.append("Package: ").append(context.getPackageName()).append("\n\n");

        sb.append("Permissions:\n");
        for (String perm : context.getPermissions()) {
            sb.append("- ").append(perm).append("\n");
        }
        sb.append("\n");

        sb.append("Components:\n");
        for (String comp : context.getComponents()) {
            sb.append("- ").append(comp).append("\n");
        }
        sb.append("\n");

        sb.append("Security Analysis:\n").append(context.getSecurityReport()).append("\n\n");

        sb.append("Please provide:\n");
        sb.append("1. A summary of what this application does\n");
        sb.append("2. Key features based on components and permissions\n");
        sb.append("3. Security concerns\n");
        return sb.toString();
    }

    public String buildSecurityReviewPrompt() throws AndrolibException {
        AiContext context = buildContext();
        StringBuilder sb = new StringBuilder();
        sb.append("Perform a security review of the following Android application:\n\n");
        sb.append("Package: ").append(context.getPackageName()).append("\n");
        sb.append("Automated Security Report:\n").append(context.getSecurityReport()).append("\n\n");

        sb.append("Permissions:\n");
        for (String perm : context.getPermissions()) {
            sb.append("- ").append(perm).append("\n");
        }
        sb.append("\n");

        sb.append("Exported Components:\n");
        for (String comp : context.getComponents()) {
            if (comp.contains("exported")) sb.append("- ").append(comp).append("\n");
        }
        sb.append("\n");

        sb.append("Please identify:\n");
        sb.append("1. Critical security vulnerabilities\n");
        sb.append("2. Privacy concerns from permissions\n");
        sb.append("3. Attack surface from exported components\n");
        sb.append("4. Recommendations for fixing each issue\n");
        return sb.toString();
    }

    public String buildSummarizePrompt() throws AndrolibException {
        AiContext context = buildContext();
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a concise technical summary of this Android application:\n\n");
        sb.append("APK: ").append(context.getApkFileName()).append("\n");
        sb.append("Package: ").append(context.getPackageName()).append("\n");
        sb.append("Permissions: ").append(context.getPermissions().size()).append("\n");
        sb.append("Components: ").append(context.getComponents().size()).append("\n");
        sb.append("Security Report:\n").append(context.getSecurityReport()).append("\n\n");

        sb.append("Provide a 3-5 sentence technical summary.\n");
        return sb.toString();
    }
}
