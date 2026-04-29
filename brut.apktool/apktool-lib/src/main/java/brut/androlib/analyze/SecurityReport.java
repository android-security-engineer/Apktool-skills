package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class SecurityReport {
    private List<String> dangerousPermissions = new ArrayList<>();
    private List<String> highRiskComponents = new ArrayList<>();
    private boolean debuggable;
    private boolean allowBackup;
    private boolean usesCleartextTraffic;
    private List<String> findings = new ArrayList<>();
    private int riskScore;

    public List<String> getDangerousPermissions() { return dangerousPermissions; }
    public void setDangerousPermissions(List<String> dangerousPermissions) { this.dangerousPermissions = dangerousPermissions; }
    public List<String> getHighRiskComponents() { return highRiskComponents; }
    public void setHighRiskComponents(List<String> highRiskComponents) { this.highRiskComponents = highRiskComponents; }
    public boolean isDebuggable() { return debuggable; }
    public void setDebuggable(boolean debuggable) { this.debuggable = debuggable; }
    public boolean isAllowBackup() { return allowBackup; }
    public void setAllowBackup(boolean allowBackup) { this.allowBackup = allowBackup; }
    public boolean isUsesCleartextTraffic() { return usesCleartextTraffic; }
    public void setUsesCleartextTraffic(boolean usesCleartextTraffic) { this.usesCleartextTraffic = usesCleartextTraffic; }
    public List<String> getFindings() { return findings; }
    public void setFindings(List<String> findings) { this.findings = findings; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
}
