package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class DiffResult {
    private List<String> addedPermissions = new ArrayList<>();
    private List<String> removedPermissions = new ArrayList<>();
    private List<String> addedActivities = new ArrayList<>();
    private List<String> removedActivities = new ArrayList<>();
    private List<String> addedServices = new ArrayList<>();
    private List<String> removedServices = new ArrayList<>();
    private List<String> addedDexFiles = new ArrayList<>();
    private List<String> removedDexFiles = new ArrayList<>();
    private List<String> addedNativeLibs = new ArrayList<>();
    private List<String> removedNativeLibs = new ArrayList<>();
    private String versionCodeChange;
    private String versionNameChange;
    private String targetSdkChange;

    public List<String> getAddedPermissions() { return addedPermissions; }
    public void setAddedPermissions(List<String> addedPermissions) { this.addedPermissions = addedPermissions; }
    public List<String> getRemovedPermissions() { return removedPermissions; }
    public void setRemovedPermissions(List<String> removedPermissions) { this.removedPermissions = removedPermissions; }
    public List<String> getAddedActivities() { return addedActivities; }
    public void setAddedActivities(List<String> addedActivities) { this.addedActivities = addedActivities; }
    public List<String> getRemovedActivities() { return removedActivities; }
    public void setRemovedActivities(List<String> removedActivities) { this.removedActivities = removedActivities; }
    public List<String> getAddedServices() { return addedServices; }
    public void setAddedServices(List<String> addedServices) { this.addedServices = addedServices; }
    public List<String> getRemovedServices() { return removedServices; }
    public void setRemovedServices(List<String> removedServices) { this.removedServices = removedServices; }
    public List<String> getAddedDexFiles() { return addedDexFiles; }
    public void setAddedDexFiles(List<String> addedDexFiles) { this.addedDexFiles = addedDexFiles; }
    public List<String> getRemovedDexFiles() { return removedDexFiles; }
    public void setRemovedDexFiles(List<String> removedDexFiles) { this.removedDexFiles = removedDexFiles; }
    public List<String> getAddedNativeLibs() { return addedNativeLibs; }
    public void setAddedNativeLibs(List<String> addedNativeLibs) { this.addedNativeLibs = addedNativeLibs; }
    public List<String> getRemovedNativeLibs() { return removedNativeLibs; }
    public void setRemovedNativeLibs(List<String> removedNativeLibs) { this.removedNativeLibs = removedNativeLibs; }
    public String getVersionCodeChange() { return versionCodeChange; }
    public void setVersionCodeChange(String versionCodeChange) { this.versionCodeChange = versionCodeChange; }
    public String getVersionNameChange() { return versionNameChange; }
    public void setVersionNameChange(String versionNameChange) { this.versionNameChange = versionNameChange; }
    public String getTargetSdkChange() { return targetSdkChange; }
    public void setTargetSdkChange(String targetSdkChange) { this.targetSdkChange = targetSdkChange; }
}
