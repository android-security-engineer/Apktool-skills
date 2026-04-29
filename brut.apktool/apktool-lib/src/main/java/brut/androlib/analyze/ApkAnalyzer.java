package brut.androlib.analyze;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.meta.ApkInfo;
import brut.androlib.meta.SdkInfo;
import brut.androlib.meta.VersionInfo;
import brut.androlib.res.ResDecoder;
import brut.androlib.res.decoder.BinaryXmlResourceParser;
import brut.androlib.res.decoder.ManifestPullEventHandler;
import brut.androlib.res.decoder.ResXmlPullStreamDecoder;
import brut.androlib.res.table.ResEntry;
import brut.androlib.res.table.ResPackage;
import brut.androlib.res.table.ResTable;
import brut.androlib.res.table.ResType;
import brut.androlib.res.xml.ResXmlSerializer;
import brut.directory.Directory;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApkAnalyzer {
    private static final Pattern DEX_PATTERN = Pattern.compile("classes([2-9]|[1-9][0-9]+)?\\.dex");
    private static final Pattern MANIFEST_PERMISSION = Pattern.compile(
        "<uses-permission\\s+[^>]*android:name=\"([^\"]+)\"");
    private static final Pattern MANIFEST_COMPONENT = Pattern.compile(
        "<(activity|service|receiver|provider)\\s+[^>]*android:name=\"([^\"]+)\"");
    private static final Pattern MANIFEST_EXPORTED = Pattern.compile(
        "android:exported=\"(true|false)\"");
    private static final Pattern MANIFEST_DEBUGGABLE = Pattern.compile(
        "android:debuggable=\"(true|false)\"");
    private static final Pattern MANIFEST_ALLOW_BACKUP = Pattern.compile(
        "android:allowBackup=\"(true|false)\"");
    private static final Pattern MANIFEST_PACKAGE = Pattern.compile(
        "package=\"([^\"]+)\"");

    private static final Set<String> DANGEROUS_PERMISSIONS = new HashSet<>(Arrays.asList(
        "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS",
        "android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR",
        "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE",
        "android.permission.READ_SMS", "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS", "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO", "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.READ_PHONE_NUMBERS", "android.permission.ANSWER_PHONE_CALLS",
        "android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_SCAN"
    ));

    private final ExtFile mApkFile;
    private final Config mConfig;

    public ApkAnalyzer(File apkFile, Config config) {
        mApkFile = new ExtFile(apkFile);
        mConfig = config;
    }

    public ApkSummary getSummary() throws AndrolibException {
        ApkSummary summary = new ApkSummary();
        summary.setFileName(mApkFile.getName());
        summary.setFileSize(mApkFile.length());

        try {
            Directory dir = mApkFile.getDirectory();

            int dexCount = 0;
            for (String file : dir.getFiles(true)) {
                if (file.equals("classes.dex") || DEX_PATTERN.matcher(file).matches()) {
                    dexCount++;
                }
            }
            summary.setDexCount(dexCount);
            summary.setHasResources(dir.containsFile("resources.arsc"));
            summary.setHasAssets(dir.containsDir("assets"));
            summary.setHasNativeLibs(dir.containsDir("lib"));

            List<String> archs = new ArrayList<>();
            if (dir.containsDir("lib")) {
                for (String subDir : dir.getDir("lib").getFiles(false)) {
                    archs.add(subDir);
                }
            }
            summary.setArchitectures(archs);

            ManifestInfo manifest = getManifestInfo();
            if (manifest != null) {
                summary.setPackageName(manifest.getPackageName());
                summary.setVersionName(manifest.getVersionName());
                summary.setVersionCode(manifest.getVersionCode());
                summary.setMinSdkVersion(manifest.getMinSdkVersion());
                summary.setTargetSdkVersion(manifest.getTargetSdkVersion());
                summary.setPermissionCount(manifest.getPermissions().size());
                summary.setActivityCount(manifest.getActivities().size());
                summary.setServiceCount(manifest.getServices().size());
                summary.setReceiverCount(manifest.getReceivers().size());
                summary.setProviderCount(manifest.getProviders().size());
            }
        } catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        return summary;
    }

    public ManifestInfo getManifestInfo() throws AndrolibException {
        ManifestInfo info = new ManifestInfo();

        try {
            Directory dir = mApkFile.getDirectory();
            if (!dir.containsFile("AndroidManifest.xml")) {
                return null;
            }

            ApkInfo apkInfo = new ApkInfo();
            apkInfo.setApkFile(mApkFile);
            ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
            BinaryXmlResourceParser parser = new BinaryXmlResourceParser(
                resDecoder.getTable(), false, false);
            ResXmlSerializer serial = new ResXmlSerializer(true);
            ManifestPullEventHandler handler = new ManifestPullEventHandler(apkInfo, true);
            ResXmlPullStreamDecoder decoder = new ResXmlPullStreamDecoder(parser, serial, handler);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream in = dir.getFileInput("AndroidManifest.xml")) {
                decoder.decode(in, baos);
            }

            String manifestXml = baos.toString("UTF-8");
            parseManifestFields(manifestXml, info);

            SdkInfo sdkInfo = apkInfo.getSdkInfo();
            if (sdkInfo.getMinSdkVersion() != null) {
                info.setMinSdkVersion(sdkInfo.getMinSdkVersion());
            }
            if (sdkInfo.getTargetSdkVersion() != null) {
                info.setTargetSdkVersion(sdkInfo.getTargetSdkVersion());
            }

            VersionInfo versionInfo = apkInfo.getVersionInfo();
            if (versionInfo.getVersionName() != null) {
                info.setVersionName(versionInfo.getVersionName());
            }
            info.setVersionCode(versionInfo.getVersionCode());

        } catch (DirectoryException | IOException ex) {
            throw new AndrolibException(ex);
        }

        return info;
    }

    private void parseManifestFields(String xml, ManifestInfo info) {
        Matcher m = MANIFEST_PACKAGE.matcher(xml);
        if (m.find()) info.setPackageName(m.group(1));

        m = MANIFEST_PERMISSION.matcher(xml);
        while (m.find()) {
            info.getPermissions().add(m.group(1));
        }

        m = MANIFEST_COMPONENT.matcher(xml);
        while (m.find()) {
            String compType = m.group(1);
            String compName = m.group(2);
            ComponentInfo comp = new ComponentInfo(compName, compType);

            // Look for exported attribute in a nearby context
            int tagStart = xml.lastIndexOf('<', m.start());
            int tagEnd = xml.indexOf('>', m.end());
            if (tagEnd < 0) tagEnd = Math.min(xml.length(), m.end() + 200);
            if (tagStart < 0) tagStart = Math.max(0, m.start() - 200);
            String tagContent = xml.substring(tagStart, tagEnd);
            Matcher expMatcher = MANIFEST_EXPORTED.matcher(tagContent);
            if (expMatcher.find()) {
                comp.setExported("true".equals(expMatcher.group(1)));
            }

            switch (compType) {
                case "activity": info.getActivities().add(comp); break;
                case "service": info.getServices().add(comp); break;
                case "receiver": info.getReceivers().add(comp); break;
                case "provider": info.getProviders().add(comp); break;
            }
        }

        m = MANIFEST_DEBUGGABLE.matcher(xml);
        if (m.find()) info.setDebuggable("true".equals(m.group(1)));

        m = MANIFEST_ALLOW_BACKUP.matcher(xml);
        if (m.find()) info.setAllowBackup("true".equals(m.group(1)));
    }

    public SecurityReport getSecurityReport() throws AndrolibException {
        SecurityReport report = new SecurityReport();
        ManifestInfo manifest = getManifestInfo();
        if (manifest == null) return report;

        for (String perm : manifest.getPermissions()) {
            if (DANGEROUS_PERMISSIONS.contains(perm)) {
                report.getDangerousPermissions().add(perm);
            }
        }

        List<List<ComponentInfo>> allComponents = Arrays.asList(
            manifest.getActivities(), manifest.getServices(),
            manifest.getReceivers(), manifest.getProviders()
        );
        for (List<ComponentInfo> components : allComponents) {
            for (ComponentInfo comp : components) {
                if (comp.isExported() && comp.getPermissions().isEmpty()) {
                    report.getHighRiskComponents().add(comp.getType() + ": " + comp.getName());
                }
            }
        }

        report.setDebuggable(manifest.isDebuggable());
        report.setAllowBackup(manifest.isAllowBackup());

        if (manifest.isDebuggable()) {
            report.getFindings().add("HIGH: Application is debuggable - android:debuggable=true");
        }
        if (manifest.isAllowBackup()) {
            report.getFindings().add("MEDIUM: Application allows backup - android:allowBackup=true");
        }
        if (!report.getDangerousPermissions().isEmpty()) {
            report.getFindings().add("MEDIUM: Application requests " + report.getDangerousPermissions().size() + " dangerous permissions");
        }
        if (!report.getHighRiskComponents().isEmpty()) {
            report.getFindings().add("HIGH: " + report.getHighRiskComponents().size() + " exported components without permission protection");
        }

        int score = 0;
        score += report.isDebuggable() ? 30 : 0;
        score += report.isAllowBackup() ? 10 : 0;
        score += Math.min(20, report.getDangerousPermissions().size() * 2);
        score += Math.min(30, report.getHighRiskComponents().size() * 5);
        report.setRiskScore(Math.min(100, score));

        return report;
    }

    public ResourceSummary getResourceSummary() throws AndrolibException {
        ResourceSummary summary = new ResourceSummary();

        ApkInfo apkInfo = new ApkInfo();
        apkInfo.setApkFile(mApkFile);
        ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
        ResTable table = resDecoder.getTable();
        table.load();

        ResPackage pkg = table.getMainPackage();
        if (pkg != null) {
            summary.setPackageName(pkg.getName());
            summary.setPackageId(pkg.getId());

            int total = 0;
            for (ResEntry entry : pkg.listEntries()) {
                ResType type = entry.getType();
                String typeName = type.getName();
                Integer count = summary.getTypeCounts().get(typeName);
                summary.getTypeCounts().put(typeName, count != null ? count + 1 : 1);
                total++;
            }
            summary.setTotalEntries(total);
        }

        return summary;
    }

    public ApiSurfaceInfo getApiSurface() throws AndrolibException {
        ApiSurfaceInfo surface = new ApiSurfaceInfo();
        ManifestInfo manifest = getManifestInfo();
        if (manifest == null) return surface;

        for (ComponentInfo comp : manifest.getActivities()) {
            if (comp.isExported()) surface.getExportedActivities().add(comp);
        }
        for (ComponentInfo comp : manifest.getServices()) {
            if (comp.isExported()) surface.getExportedServices().add(comp);
        }
        for (ComponentInfo comp : manifest.getReceivers()) {
            if (comp.isExported()) surface.getExportedReceivers().add(comp);
        }
        for (ComponentInfo comp : manifest.getProviders()) {
            if (comp.isExported()) surface.getExportedProviders().add(comp);
        }

        collectIntentFilters(manifest.getActivities(), "activity", surface.getIntentFilters());
        collectIntentFilters(manifest.getServices(), "service", surface.getIntentFilters());
        collectIntentFilters(manifest.getReceivers(), "receiver", surface.getIntentFilters());

        surface.setTotalExportedComponents(
            surface.getExportedActivities().size() +
            surface.getExportedServices().size() +
            surface.getExportedReceivers().size() +
            surface.getExportedProviders().size()
        );

        return surface;
    }

    private void collectIntentFilters(List<ComponentInfo> components, String type, List<ApiSurfaceInfo.IntentFilterInfo> filters) {
        for (ComponentInfo comp : components) {
            if (!comp.getIntentFilters().isEmpty()) {
                ApiSurfaceInfo.IntentFilterInfo filter = new ApiSurfaceInfo.IntentFilterInfo();
                filter.setComponent(comp.getName());
                filter.setComponentType(type);
                filter.setActions(comp.getIntentFilters());
                filters.add(filter);
            }
        }
    }

    public java.util.Map<String, List<ComponentInfo>> getAllComponents() throws AndrolibException {
        java.util.Map<String, List<ComponentInfo>> result = new java.util.LinkedHashMap<>();
        ManifestInfo manifest = getManifestInfo();
        if (manifest == null) return result;
        result.put("activities", manifest.getActivities());
        result.put("services", manifest.getServices());
        result.put("receivers", manifest.getReceivers());
        result.put("providers", manifest.getProviders());
        return result;
    }
}
