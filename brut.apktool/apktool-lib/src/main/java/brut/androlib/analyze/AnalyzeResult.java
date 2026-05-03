package brut.androlib.analyze;

import java.util.Map;

public class AnalyzeResult {
    private ApkSummary summary;
    private ManifestInfo manifest;
    private SecurityReport security;
    private ApiSurfaceInfo apiSurface;
    private ResourceSummary resources;
    private SigningInfo signing;
    private StructureInfo structure;
    private Map<String, Map<String, Integer>> dexInfo;
    private Map<String, Object> nativeLibs;

    public ApkSummary getSummary() { return summary; }
    public void setSummary(ApkSummary summary) { this.summary = summary; }
    public ManifestInfo getManifest() { return manifest; }
    public void setManifest(ManifestInfo manifest) { this.manifest = manifest; }
    public SecurityReport getSecurity() { return security; }
    public void setSecurity(SecurityReport security) { this.security = security; }
    public ApiSurfaceInfo getApiSurface() { return apiSurface; }
    public void setApiSurface(ApiSurfaceInfo apiSurface) { this.apiSurface = apiSurface; }
    public ResourceSummary getResources() { return resources; }
    public void setResources(ResourceSummary resources) { this.resources = resources; }
    public SigningInfo getSigning() { return signing; }
    public void setSigning(SigningInfo signing) { this.signing = signing; }
    public StructureInfo getStructure() { return structure; }
    public void setStructure(StructureInfo structure) { this.structure = structure; }
    public Map<String, Map<String, Integer>> getDexInfo() { return dexInfo; }
    public void setDexInfo(Map<String, Map<String, Integer>> dexInfo) { this.dexInfo = dexInfo; }
    public Map<String, Object> getNativeLibs() { return nativeLibs; }
    public void setNativeLibs(Map<String, Object> nativeLibs) { this.nativeLibs = nativeLibs; }
}