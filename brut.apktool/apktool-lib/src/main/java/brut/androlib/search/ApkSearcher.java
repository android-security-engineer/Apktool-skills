package brut.androlib.search;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.res.ResDecoder;
import brut.androlib.res.table.ResEntry;
import brut.androlib.res.table.ResPackage;
import brut.androlib.res.table.ResTable;
import brut.androlib.res.table.value.ResString;
import brut.androlib.res.table.value.ResValue;
import brut.androlib.meta.ApkInfo;
import brut.directory.ExtFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer;
import com.android.tools.smali.dexlib2.iface.ClassDef;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class ApkSearcher {
    private final ExtFile mApkFile;
    private final Config mConfig;

    public ApkSearcher(File apkFile, Config config) {
        mApkFile = new ExtFile(apkFile);
        mConfig = config;
    }

    public SearchResult searchStrings(String pattern) throws AndrolibException {
        SearchResult result = new SearchResult();
        result.setQuery(pattern);
        result.setType("strings");

        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        try {
            ApkInfo apkInfo = new ApkInfo();
            apkInfo.setApkFile(mApkFile);
            ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
            ResTable table = resDecoder.getTable();
            table.load();

            ResPackage pkg = table.getMainPackage();
            if (pkg != null) {
                for (ResEntry entry : pkg.listEntries()) {
                    ResValue value = entry.getValue();
                    if (value instanceof ResString) {
                        String strValue = ((ResString) value).getValue().toString();
                        if (strValue != null && regex.matcher(strValue).find()) {
                            result.getMatches().add(new SearchResult.SearchMatch(
                                entry.getType().getName() + "/" + entry.getSpec().getName(),
                                strValue,
                                "resources"
                            ));
                        }
                    }
                }
            }
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        result.setTotalMatches(result.getMatches().size());
        return result;
    }

    public SearchResult searchClasses(String pattern) throws AndrolibException {
        SearchResult result = new SearchResult();
        result.setQuery(pattern);
        result.setType("classes");

        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        try {
            ZipDexContainer container = new ZipDexContainer(mApkFile, null);
            for (String dexName : container.getDexEntryNames()) {
                ZipDexContainer.DexEntry<DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;

                DexBackedDexFile dexFile = entry.getDexFile();
                for (ClassDef classDef : dexFile.getClasses()) {
                    String className = classDef.getType();
                    String humanName = className.substring(1, className.length() - 1).replace('/', '.');
                    if (regex.matcher(humanName).find()) {
                        result.getMatches().add(new SearchResult.SearchMatch(
                            humanName,
                            className,
                            dexName
                        ));
                    }
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        result.setTotalMatches(result.getMatches().size());
        return result;
    }

    public SearchResult searchMethods(String pattern) throws AndrolibException {
        SearchResult result = new SearchResult();
        result.setQuery(pattern);
        result.setType("methods");

        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        try {
            ZipDexContainer container = new ZipDexContainer(mApkFile, null);
            for (String dexName : container.getDexEntryNames()) {
                ZipDexContainer.DexEntry<DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;

                DexBackedDexFile dexFile = entry.getDexFile();
                for (ClassDef classDef : dexFile.getClasses()) {
                    String className = classDef.getType()
                        .substring(1, classDef.getType().length() - 1).replace('/', '.');
                    for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods()) {
                        if (regex.matcher(method.getName()).find()) {
                            result.getMatches().add(new SearchResult.SearchMatch(
                                className + "." + method.getName(),
                                method.getName(),
                                dexName
                            ));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        result.setTotalMatches(result.getMatches().size());
        return result;
    }
}
