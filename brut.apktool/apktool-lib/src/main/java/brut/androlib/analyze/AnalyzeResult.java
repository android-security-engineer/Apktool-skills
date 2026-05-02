package brut.androlib.analyze;

public class AnalyzeResult {
    private ApkSummary summary;
    private ManifestInfo manifest;
    private SecurityReport security;
    private ApiSurfaceInfo apiSurface;
    private ResourceSummary resources;
    private SigningInfo signing;
    private StructureInfo structure;

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
}
