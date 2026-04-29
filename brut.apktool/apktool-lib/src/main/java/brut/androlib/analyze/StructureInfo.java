package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructureInfo {
    private int totalClasses;
    private int totalMethods;
    private int totalFields;
    private Map<String, Integer> packageCounts = new LinkedHashMap<>();
    private List<String> topClasses = new ArrayList<>();
    private int dexCount;
    private Map<String, Integer> dexClassCounts = new LinkedHashMap<>();

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    public int getTotalMethods() { return totalMethods; }
    public void setTotalMethods(int totalMethods) { this.totalMethods = totalMethods; }
    public int getTotalFields() { return totalFields; }
    public void setTotalFields(int totalFields) { this.totalFields = totalFields; }
    public Map<String, Integer> getPackageCounts() { return packageCounts; }
    public void setPackageCounts(Map<String, Integer> packageCounts) { this.packageCounts = packageCounts; }
    public List<String> getTopClasses() { return topClasses; }
    public void setTopClasses(List<String> topClasses) { this.topClasses = topClasses; }
    public int getDexCount() { return dexCount; }
    public void setDexCount(int dexCount) { this.dexCount = dexCount; }
    public Map<String, Integer> getDexClassCounts() { return dexClassCounts; }
    public void setDexClassCounts(Map<String, Integer> dexClassCounts) { this.dexClassCounts = dexClassCounts; }
}
