package brut.androlib.analyze;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.directory.ExtFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer;
import com.android.tools.smali.dexlib2.iface.ClassDef;

import java.io.File;
import java.io.IOException;
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
        ExtFile extFile = new ExtFile(apkFile);

        try {
            ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
            ApkSummary summary = analyzer.getSummary();
            info.setDexCount(summary.getDexCount());

            ZipDexContainer container = new ZipDexContainer(extFile, null);
            int totalClasses = 0;
            int totalMethods = 0;
            int totalFields = 0;
            Map<String, Integer> packageCounts = new LinkedHashMap<>();
            List<String> topClasses = new ArrayList<>();
            Map<String, Integer> dexClassCounts = new LinkedHashMap<>();

            for (String dexName : container.getDexEntryNames()) {
                ZipDexContainer.DexEntry<DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;

                DexBackedDexFile dexFile = entry.getDexFile();
                int dexClassCount = 0;

                for (ClassDef classDef : dexFile.getClasses()) {
                    dexClassCount++;
                    totalClasses++;

                    String className = classDef.getType();
                    String humanName = className.substring(1, className.length() - 1).replace('/', '.');

                    int lastDot = humanName.lastIndexOf('.');
                    if (lastDot > 0) {
                        String pkg = humanName.substring(0, lastDot);
                        packageCounts.merge(pkg, 1, Integer::sum);
                    }

                    int methodCount = 0;
                    for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods()) {
                        methodCount++;
                    }
                    totalMethods += methodCount;

                    int fieldCount = 0;
                    for (com.android.tools.smali.dexlib2.iface.Field field : classDef.getFields()) {
                        fieldCount++;
                    }
                    totalFields += fieldCount;

                    topClasses.add(humanName + " (" + methodCount + " methods, " + fieldCount + " fields)");
                }

                dexClassCounts.put(dexName, dexClassCount);
            }

            info.setTotalClasses(totalClasses);
            info.setTotalMethods(totalMethods);
            info.setTotalFields(totalFields);
            info.setPackageCounts(packageCounts);
            topClasses.sort((a, b) -> {
                int ma = Integer.parseInt(a.replaceAll(".*\\((\\d+) methods.*", "$1"));
                int mb = Integer.parseInt(b.replaceAll(".*\\((\\d+) methods.*", "$1"));
                return mb - ma;
            });
            info.setTopClasses(topClasses.subList(0, Math.min(20, topClasses.size())));
            info.setDexClassCounts(dexClassCounts);

        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }

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
