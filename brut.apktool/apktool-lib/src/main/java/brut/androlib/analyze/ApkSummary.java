package brut.androlib.analyze;

import java.util.List;

public class ApkSummary {
    private String fileName;
    private long fileSize;
    private String packageName;
    private String versionName;
    private int versionCode;
    private String minSdkVersion;
    private String targetSdkVersion;
    private int dexCount;
    private boolean hasResources;
    private boolean hasAssets;
    private boolean hasNativeLibs;
    private List<String> architectures;
    private int permissionCount;
    private int activityCount;
    private int serviceCount;
    private int receiverCount;
    private int providerCount;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
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
    public int getDexCount() { return dexCount; }
    public void setDexCount(int dexCount) { this.dexCount = dexCount; }
    public boolean isHasResources() { return hasResources; }
    public void setHasResources(boolean hasResources) { this.hasResources = hasResources; }
    public boolean isHasAssets() { return hasAssets; }
    public void setHasAssets(boolean hasAssets) { this.hasAssets = hasAssets; }
    public boolean isHasNativeLibs() { return hasNativeLibs; }
    public void setHasNativeLibs(boolean hasNativeLibs) { this.hasNativeLibs = hasNativeLibs; }
    public List<String> getArchitectures() { return architectures; }
    public void setArchitectures(List<String> architectures) { this.architectures = architectures; }
    public int getPermissionCount() { return permissionCount; }
    public void setPermissionCount(int permissionCount) { this.permissionCount = permissionCount; }
    public int getActivityCount() { return activityCount; }
    public void setActivityCount(int activityCount) { this.activityCount = activityCount; }
    public int getServiceCount() { return serviceCount; }
    public void setServiceCount(int serviceCount) { this.serviceCount = serviceCount; }
    public int getReceiverCount() { return receiverCount; }
    public void setReceiverCount(int receiverCount) { this.receiverCount = receiverCount; }
    public int getProviderCount() { return providerCount; }
    public void setProviderCount(int providerCount) { this.providerCount = providerCount; }
}
