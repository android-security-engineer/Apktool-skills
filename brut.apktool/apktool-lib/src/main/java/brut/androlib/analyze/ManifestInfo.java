package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class ManifestInfo {
    private String packageName;
    private String versionName;
    private int versionCode;
    private String minSdkVersion;
    private String targetSdkVersion;
    private String maxSdkVersion;
    private List<String> permissions = new ArrayList<>();
    private List<ComponentInfo> activities = new ArrayList<>();
    private List<ComponentInfo> services = new ArrayList<>();
    private List<ComponentInfo> receivers = new ArrayList<>();
    private List<ComponentInfo> providers = new ArrayList<>();
    private List<String> usesLibraries = new ArrayList<>();
    private boolean debuggable;
    private boolean allowBackup;
    private String networkSecurityConfig;

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }
    public int getVersionCode() { return versionCode; }
    public void setVersionCode(int versionCode) { this.versionCode = versionCode; }
    public String getMinSdkVersion() { return minSdkVersion; }
    public void setMinSdkVersion(String minSdkVersion) { this.minSdkVersion = minSdkVersion; }
    public String getTargetSdkVersion() { return targetSdkVersion; }
    public void setTargetSdkVersion(String targetSdkVersion) { this.targetSdkVersion = targetSdkVersion; }
    public String getMaxSdkVersion() { return maxSdkVersion; }
    public void setMaxSdkVersion(String maxSdkVersion) { this.maxSdkVersion = maxSdkVersion; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public List<ComponentInfo> getActivities() { return activities; }
    public void setActivities(List<ComponentInfo> activities) { this.activities = activities; }
    public List<ComponentInfo> getServices() { return services; }
    public void setServices(List<ComponentInfo> services) { this.services = services; }
    public List<ComponentInfo> getReceivers() { return receivers; }
    public void setReceivers(List<ComponentInfo> receivers) { this.receivers = receivers; }
    public List<ComponentInfo> getProviders() { return providers; }
    public void setProviders(List<ComponentInfo> providers) { this.providers = providers; }
    public List<String> getUsesLibraries() { return usesLibraries; }
    public void setUsesLibraries(List<String> usesLibraries) { this.usesLibraries = usesLibraries; }
    public boolean isDebuggable() { return debuggable; }
    public void setDebuggable(boolean debuggable) { this.debuggable = debuggable; }
    public boolean isAllowBackup() { return allowBackup; }
    public void setAllowBackup(boolean allowBackup) { this.allowBackup = allowBackup; }
    public String getNetworkSecurityConfig() { return networkSecurityConfig; }
    public void setNetworkSecurityConfig(String networkSecurityConfig) { this.networkSecurityConfig = networkSecurityConfig; }
}
