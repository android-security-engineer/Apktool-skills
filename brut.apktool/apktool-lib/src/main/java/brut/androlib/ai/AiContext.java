package brut.androlib.ai;

import java.util.ArrayList;
import java.util.List;

public class AiContext {
    private String apkFileName;
    private String packageName;
    private String manifestXml;
    private List<String> permissions = new ArrayList<>();
    private List<String> components = new ArrayList<>();
    private List<String> stringResources = new ArrayList<>();
    private String securityReport;
    private int estimatedTokenCount;

    public String getApkFileName() { return apkFileName; }
    public void setApkFileName(String apkFileName) { this.apkFileName = apkFileName; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getManifestXml() { return manifestXml; }
    public void setManifestXml(String manifestXml) { this.manifestXml = manifestXml; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public List<String> getComponents() { return components; }
    public void setComponents(List<String> components) { this.components = components; }
    public List<String> getStringResources() { return stringResources; }
    public void setStringResources(List<String> stringResources) { this.stringResources = stringResources; }
    public String getSecurityReport() { return securityReport; }
    public void setSecurityReport(String securityReport) { this.securityReport = securityReport; }
    public int getEstimatedTokenCount() { return estimatedTokenCount; }
    public void setEstimatedTokenCount(int estimatedTokenCount) { this.estimatedTokenCount = estimatedTokenCount; }
}
