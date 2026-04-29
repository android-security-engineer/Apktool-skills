package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceSummary {
    private String packageName;
    private int packageId;
    private Map<String, Integer> typeCounts = new LinkedHashMap<>();
    private List<String> locales = new ArrayList<>();
    private int totalEntries;

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }
    public Map<String, Integer> getTypeCounts() { return typeCounts; }
    public void setTypeCounts(Map<String, Integer> typeCounts) { this.typeCounts = typeCounts; }
    public List<String> getLocales() { return locales; }
    public void setLocales(List<String> locales) { this.locales = locales; }
    public int getTotalEntries() { return totalEntries; }
    public void setTotalEntries(int totalEntries) { this.totalEntries = totalEntries; }
}
