package brut.androlib.output;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {
    private static final Map<String, CommandInfo> COMMANDS = new LinkedHashMap<>();

    static {
        // === Core Commands ===
        register("decode", "d", "Decode an APK file to a directory of smali and resources",
            "apktool d [options] <apk-file>", "Directory structure with smali/, res/, AndroidManifest.xml, apktool.yml",
            "core", new String[]{"apktool d app.apk", "apktool d app.apk -o output_dir", "apktool d app.apk -s (no source decode)"});

        register("build", "b", "Build an APK from a decoded directory",
            "apktool b [options] <apk-dir>", "Built APK file in dist/ directory",
            "core", new String[]{"apktool b app_dir", "apktool b app_dir -o custom.apk"});

        register("install-framework", "if", "Install a framework APK for resource decoding",
            "apktool if [options] <apk-file>", "Framework file installed to framework directory",
            "core", new String[]{"apktool if framework-res.apk"});

        register("clean-frameworks", "cf", "Remove installed framework files",
            "apktool cf [options]", "Framework files removed",
            "core", new String[]{"apktool cf"});

        register("list-frameworks", "lf", "List installed framework files",
            "apktool lf [options]", "List of framework file names",
            "core", new String[]{"apktool lf"});

        register("publicize-resources", "pr", "Make all resources public in an ARSC file",
            "apktool pr <arsc-file>", "Modified ARSC file",
            "core", new String[]{"apktool pr resources.arsc"});

        // === AI Analysis Commands ===
        register("info", null, "Get APK metadata summary: package name, version, file size, component counts",
            "apktool info <apk-file>", "JSON: {fileName, fileSize, packageName, versionName, versionCode, minSdkVersion, targetSdkVersion, dexCount, hasResources, hasAssets, hasNativeLibs, architectures, permissionCount, activityCount, serviceCount, receiverCount, providerCount}",
            "analysis", new String[]{"apktool info app.apk"});

        register("manifest", null, "Get decoded AndroidManifest.xml as structured JSON: all components, permissions, SDK info, flags",
            "apktool manifest <apk-file>", "JSON: {packageName, versionName, versionCode, minSdkVersion, targetSdkVersion, permissions[], activities[{name,exported,intentFilters[],permissions[]}], services[], receivers[], providers[], debuggable, allowBackup}",
            "analysis", new String[]{"apktool manifest app.apk"});

        register("permissions", null, "Get list of all permissions declared in AndroidManifest.xml",
            "apktool permissions <apk-file>", "JSON array of permission strings",
            "analysis", new String[]{"apktool permissions app.apk"});

        register("activities", null, "Get list of all Activity components with export status and intent filters",
            "apktool activities <apk-file>", "JSON array: [{name, exported, intentFilters[], permissions[]}]",
            "analysis", new String[]{"apktool activities app.apk"});

        register("services", null, "Get list of all Service components with export status",
            "apktool services <apk-file>", "JSON array: [{name, exported, intentFilters[], permissions[]}]",
            "analysis", new String[]{"apktool services app.apk"});

        register("receivers", null, "Get list of all BroadcastReceiver components with export status",
            "apktool receivers <apk-file>", "JSON array: [{name, exported, intentFilters[], permissions[]}]",
            "analysis", new String[]{"apktool receivers app.apk"});

        register("providers", null, "Get list of all ContentProvider components with export status",
            "apktool providers <apk-file>", "JSON array: [{name, exported, intentFilters[], permissions[]}]",
            "analysis", new String[]{"apktool providers app.apk"});

        register("components", null, "Get all Android components (activities, services, receivers, providers) in one command",
            "apktool components <apk-file>", "JSON: {activities[], services[], receivers[], providers[]}",
            "analysis", new String[]{"apktool components app.apk"});

        register("sdk-info", null, "Get SDK version requirements (min/target/max SDK)",
            "apktool sdk-info <apk-file>", "JSON: {minSdkVersion, targetSdkVersion, maxSdkVersion}",
            "analysis", new String[]{"apktool sdk-info app.apk"});

        register("resources", null, "Get resource table summary: type counts, locales, package info",
            "apktool resources <apk-file>", "JSON: {packageName, packageId, typeCounts{typeName->count}, locales[], totalEntries}",
            "analysis", new String[]{"apktool resources app.apk"});

        register("security", null, "Get security analysis report: dangerous permissions, exported components risk, risk score",
            "apktool security <apk-file>", "JSON: {dangerousPermissions[], highRiskComponents[], debuggable, allowBackup, usesCleartextTraffic, findings[], riskScore(0-100)}",
            "analysis", new String[]{"apktool security app.apk"});

        register("api-surface", null, "Get all exported components and their intent filters - the app's public API surface",
            "apktool api-surface <apk-file>", "JSON: {exportedActivities[], exportedServices[], exportedReceivers[], exportedProviders[], intentFilters[{component,actions[],categories[],dataSchemes[]}], totalExportedComponents}",
            "analysis", new String[]{"apktool api-surface app.apk"});

        register("strings", null, "Extract all strings from DEX files and resources with optional pattern filtering",
            "apktool strings <apk-file> [pattern]", "JSON: {query, type, totalMatches, matches[{name, value, source}]}",
            "analysis", new String[]{"apktool strings app.apk", "apktool strings app.apk 'http.*'"});

        register("signing", null, "Get APK signing certificate information: signer, certificates, digests",
            "apktool signing <apk-file>", "JSON: {v1Signing, v2Signing, v3Signing, certificates[{subject, issuer, serial, notBefore, notAfter, fingerprints{sha256,sha1,md5}}]}",
            "analysis", new String[]{"apktool signing app.apk"});

        // === Search Commands ===
        register("search", null, "Search APK content: strings, classes, or methods by regex pattern",
            "apktool search <apk-file> [pattern] -t <type>", "JSON: {query, type, totalMatches, matches[{name, value, source}]}",
            "search", new String[]{"apktool search app.apk 'Activity' -t classes", "apktool search app.apk 'http.*' -t strings", "apktool search app.apk 'onCreate' -t methods"});

        // === Diff & Structure Commands ===
        register("diff", null, "Compare two APKs: find added/removed permissions, components, version changes",
            "apktool diff <apk1> <apk2>", "JSON: {addedPermissions[], removedPermissions[], addedActivities[], removedActivities[], addedServices[], removedServices[], versionCodeChange, versionNameChange, targetSdkChange}",
            "analysis", new String[]{"apktool diff app_v1.apk app_v2.apk"});

        register("structure", null, "Get code structure overview: class/method/field counts, package distribution",
            "apktool structure <apk-file>", "JSON: {totalClasses, totalMethods, totalFields, packageCounts{}, topClasses[], dexCount, dexClassCounts{}}",
            "analysis", new String[]{"apktool structure app.apk"});

        register("analyze", null, "Run comprehensive analysis: all metadata, security, API surface, signing, resources, and structure in one command",
            "apktool analyze <apk-file>", "JSON: {summary, manifest, security, apiSurface, resources, signing, structure}",
            "analysis", new String[]{"apktool analyze app.apk"});

        register("dex-list", null, "List all DEX files in the APK with count",
            "apktool dex-list <apk-file>", "JSON: {dexCount, dexFiles[]}",
            "analysis", new String[]{"apktool dex-list app.apk"});

        register("locales", null, "List all supported locales/regions from the resource table",
            "apktool locales <apk-file>", "JSON array of locale strings",
            "analysis", new String[]{"apktool locales app.apk"});

        register("native-libs", null, "List native libraries per architecture with file names",
            "apktool native-libs <apk-file>", "JSON: {hasNativeLibs, architectures[], libsByArch{arch->[libs]}}",
            "analysis", new String[]{"apktool native-libs app.apk"});

        register("dex-info", null, "Get per-DEX class/method/field statistics",
            "apktool dex-info <apk-file>", "JSON: {dexName->{classes,methods,fields}}",
            "analysis", new String[]{"apktool dex-info app.apk"});

        register("apk-info", null, "Read decoded APK metadata from apktool.yml in a decoded directory",
            "apktool apk-info <decoded-dir>", "JSON: {version, apkFileName, usesFramework, usesLibrary, sdkInfo, versionInfo, resourcesInfo, featureFlags, doNotCompress, hasSources, hasManifest, hasResources}",
            "analysis", new String[]{"apktool apk-info decoded_app_dir"});

        register("resource-packages", null, "List resource package groups with IDs, names, and sub-packages",
            "apktool resource-packages <apk-file>", "JSON: {packageName, packageId, packageGroups[{id,name,basePackageName,subPackages}], packageGroupCount}",
            "analysis", new String[]{"apktool resource-packages app.apk"});

        register("lib-frame-packages", null, "List shared library and framework package IDs from the resource table",
            "apktool lib-frame-packages <apk-file>", "JSON: {libPackageIds[], framePackageIds[]}",
            "analysis", new String[]{"apktool lib-frame-packages app.apk"});

        register("uses-libs", null, "List shared libraries declared in AndroidManifest.xml",
            "apktool uses-libs <apk-file>", "JSON array of library names",
            "analysis", new String[]{"apktool uses-libs app.apk"});

        register("manifest-flags", null, "Get manifest security flags: debuggable, allowBackup, cleartext traffic, network security config",
            "apktool manifest-flags <apk-file>", "JSON: {debuggable, allowBackup, usesCleartextTraffic, networkSecurityConfig}",
            "analysis", new String[]{"apktool manifest-flags app.apk"});

        register("version", null, "Get APK version information: package name, version code, version name",
            "apktool version <apk-file>", "JSON: {packageName, versionCode, versionName}",
            "analysis", new String[]{"apktool version app.apk"});

        register("file-list", null, "List all files in the APK with sizes and compression info",
            "apktool file-list <apk-file>", "JSON: {totalFiles, totalSize, totalCompressedSize, entries[{name,size,compressedSize,directory}]}",
            "analysis", new String[]{"apktool file-list app.apk"});

        register("file-hash", null, "Calculate SHA-256, SHA-1, and MD5 hash of the APK file",
            "apktool file-hash <apk-file>", "JSON: {sha256, sha1, md5, fileSize, fileName}",
            "analysis", new String[]{"apktool file-hash app.apk"});

        register("class-info", null, "Get detailed info about a DEX class: methods, fields, superclass, interfaces",
            "apktool class-info <apk-file> <class-name>", "JSON: {className, superClass, accessFlags, interfaces[], methods[{name,accessFlags,returnType,parameters[]}], fields[{name,accessFlags,type}]}",
            "analysis", new String[]{"apktool class-info app.apk com.example.MyActivity"});

        register("class-list", null, "List all class names from DEX files",
            "apktool class-list <apk-file>", "JSON: {totalClasses, classes[]}",
            "analysis", new String[]{"apktool class-list app.apk"});

        register("method-search", null, "Search method signatures by regex pattern with full type info",
            "apktool method-search <apk-file> [-p <pattern>]", "JSON: {totalMatches, methods[{className, methodName, returnType, parameters[], accessFlags}]}",
            "analysis", new String[]{"apktool method-search app.apk -p 'onCreate'", "apktool method-search app.apk"});

        register("field-search", null, "Search field names by regex pattern with type info",
            "apktool field-search <apk-file> [-p <pattern>]", "JSON: {totalMatches, fields[{className, fieldName, type, accessFlags}]}",
            "analysis", new String[]{"apktool field-search app.apk -p 'mContext'", "apktool field-search app.apk"});

        register("asset-list", null, "List all files in the assets/ directory",
            "apktool asset-list <apk-file>", "JSON: {hasAssets, totalAssets, assets[]}",
            "analysis", new String[]{"apktool asset-list app.apk"});

        register("dex-strings", null, "Extract all strings from DEX files (not resources)",
            "apktool dex-strings <apk-file>", "JSON: {totalStrings, strings[]}",
            "analysis", new String[]{"apktool dex-strings app.apk"});

        register("permission-detail", null, "Get detailed permission analysis with danger level and category classification",
            "apktool permission-detail <apk-file>", "JSON: {totalPermissions, dangerousCount, normalCount, customCount, permissions[{name, dangerous, category}]}",
            "analysis", new String[]{"apktool permission-detail app.apk"});

        register("inheritance", null, "Get class inheritance chain (superclass hierarchy) for a given class",
            "apktool inheritance <apk-file> <class-name>", "JSON: {className, inheritanceChain[]}",
            "analysis", new String[]{"apktool inheritance app.apk com.example.MyActivity"});

        register("manifest-xml", null, "Get the full decoded AndroidManifest.xml as text (for AI context)",
            "apktool manifest-xml <apk-file>", "JSON: {manifestXml}",
            "analysis", new String[]{"apktool manifest-xml app.apk"});

        // === Service Commands ===
        register("serve", null, "Start HTTP API server for AI agent integration",
            "apktool serve [-p <port>]", "HTTP server on specified port (default 8080)",
            "service", new String[]{"apktool serve", "apktool serve -p 9090"});

        // === AI Commands ===
        register("ai", null, "Generate LLM-ready analysis prompts or structured context for AI-powered APK review",
            "apktool ai <apk-file> -a <action>", "Text prompt (explain/security-review/summarize) or JSON context (context)",
            "ai", new String[]{"apktool ai app.apk", "apktool ai app.apk -a security-review", "apktool ai app.apk -a summarize", "apktool ai app.apk -a context"});

        register("help", "h", "Show help information. Use --format json for AI-consumable output",
            "apktool help [--format json]", "Text help or JSON command catalog",
            "general", new String[]{"apktool help", "apktool help --format json"});

        register("version", "v", "Show version information",
            "apktool version", "Version string",
            "general", new String[]{"apktool version"});
    }

    private static void register(String name, String shortName, String description, String usage, String outputFormat, String category, String[] examples) {
        CommandInfo cmd = new CommandInfo();
        cmd.setName(name);
        cmd.setShortName(shortName);
        cmd.setDescription(description);
        cmd.setUsage(usage);
        cmd.setOutputFormat(outputFormat);
        cmd.setCategory(category);
        for (String ex : examples) {
            cmd.getExamples().add(ex);
        }
        COMMANDS.put(name, cmd);
    }

    public static List<CommandInfo> getAllCommands() {
        return new ArrayList<>(COMMANDS.values());
    }

    public static CommandInfo getCommand(String name) {
        return COMMANDS.get(name);
    }

    public static List<CommandInfo> getCommandsByCategory(String category) {
        List<CommandInfo> result = new ArrayList<>();
        for (CommandInfo cmd : COMMANDS.values()) {
            if (category.equals(cmd.getCategory())) {
                result.add(cmd);
            }
        }
        return result;
    }

    public static String toJsonCatalog() {
        Map<String, Object> catalog = new LinkedHashMap<>();
        catalog.put("tool", "AI-Apktool");
        catalog.put("version", "3.0.3-SNAPSHOT");
        catalog.put("description", "AI-native Android reverse engineering platform");
        catalog.put("commands", new ArrayList<>(COMMANDS.values()));
        return brut.androlib.output.JsonOutput.toJson(catalog);
    }
}
