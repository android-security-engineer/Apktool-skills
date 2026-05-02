package brut.androlib.analyze;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;

import java.io.File;
import java.util.*;

public class ApkDiff {
    public static DiffResult diff(File apk1, File apk2, Config config) throws AndrolibException {
        DiffResult result = new DiffResult();

        ApkAnalyzer analyzer1 = new ApkAnalyzer(apk1, config);
        ApkAnalyzer analyzer2 = new ApkAnalyzer(apk2, config);

        ManifestInfo m1 = analyzer1.getManifestInfo();
        ManifestInfo m2 = analyzer2.getManifestInfo();

        if (m1 != null && m2 != null) {
            Set<String> p1 = new HashSet<>(m1.getPermissions());
            Set<String> p2 = new HashSet<>(m2.getPermissions());
            result.setAddedPermissions(findAdded(p1, p2));
            result.setRemovedPermissions(findRemoved(p1, p2));

            Set<String> a1 = extractNames(m1.getActivities());
            Set<String> a2 = extractNames(m2.getActivities());
            result.setAddedActivities(findAdded(a1, a2));
            result.setRemovedActivities(findRemoved(a1, a2));

            Set<String> s1 = extractNames(m1.getServices());
            Set<String> s2 = extractNames(m2.getServices());
            result.setAddedServices(findAdded(s1, s2));
            result.setRemovedServices(findRemoved(s1, s2));

            Set<String> r1 = extractNames(m1.getReceivers());
            Set<String> r2 = extractNames(m2.getReceivers());
            result.setAddedReceivers(findAdded(r1, r2));
            result.setRemovedReceivers(findRemoved(r1, r2));

            Set<String> pv1 = extractNames(m1.getProviders());
            Set<String> pv2 = extractNames(m2.getProviders());
            result.setAddedProviders(findAdded(pv1, pv2));
            result.setRemovedProviders(findRemoved(pv1, pv2));

            if (m1.getVersionCode() != m2.getVersionCode()) {
                result.setVersionCodeChange(m1.getVersionCode() + " -> " + m2.getVersionCode());
            }
            if (!Objects.equals(m1.getVersionName(), m2.getVersionName())) {
                result.setVersionNameChange(m1.getVersionName() + " -> " + m2.getVersionName());
            }
            if (!Objects.equals(m1.getTargetSdkVersion(), m2.getTargetSdkVersion())) {
                result.setTargetSdkChange(m1.getTargetSdkVersion() + " -> " + m2.getTargetSdkVersion());
            }
        }

        return result;
    }

    public static StructureInfo getStructure(File apkFile, Config config) throws AndrolibException {
        StructureInfo info = new StructureInfo();
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ApkSummary summary = analyzer.getSummary();
        info.setDexCount(summary.getDexCount());
        return info;
    }

    private static Set<String> extractNames(List<ComponentInfo> components) {
        Set<String> names = new HashSet<>();
        for (ComponentInfo comp : components) {
            names.add(comp.getName());
        }
        return names;
    }

    private static List<String> findAdded(Set<String> oldSet, Set<String> newSet) {
        List<String> added = new ArrayList<>();
        for (String item : newSet) {
            if (!oldSet.contains(item)) added.add(item);
        }
        Collections.sort(added);
        return added;
    }

    private static List<String> findRemoved(Set<String> oldSet, Set<String> newSet) {
        List<String> removed = new ArrayList<>();
        for (String item : oldSet) {
            if (!newSet.contains(item)) removed.add(item);
        }
        Collections.sort(removed);
        return removed;
    }
}
