package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class ApiSurfaceInfo {
    private List<ComponentInfo> exportedActivities = new ArrayList<>();
    private List<ComponentInfo> exportedServices = new ArrayList<>();
    private List<ComponentInfo> exportedReceivers = new ArrayList<>();
    private List<ComponentInfo> exportedProviders = new ArrayList<>();
    private List<IntentFilterInfo> intentFilters = new ArrayList<>();
    private int totalExportedComponents;

    public List<ComponentInfo> getExportedActivities() { return exportedActivities; }
    public void setExportedActivities(List<ComponentInfo> exportedActivities) { this.exportedActivities = exportedActivities; }
    public List<ComponentInfo> getExportedServices() { return exportedServices; }
    public void setExportedServices(List<ComponentInfo> exportedServices) { this.exportedServices = exportedServices; }
    public List<ComponentInfo> getExportedReceivers() { return exportedReceivers; }
    public void setExportedReceivers(List<ComponentInfo> exportedReceivers) { this.exportedReceivers = exportedReceivers; }
    public List<ComponentInfo> getExportedProviders() { return exportedProviders; }
    public void setExportedProviders(List<ComponentInfo> exportedProviders) { this.exportedProviders = exportedProviders; }
    public List<IntentFilterInfo> getIntentFilters() { return intentFilters; }
    public void setIntentFilters(List<IntentFilterInfo> intentFilters) { this.intentFilters = intentFilters; }
    public int getTotalExportedComponents() { return totalExportedComponents; }
    public void setTotalExportedComponents(int totalExportedComponents) { this.totalExportedComponents = totalExportedComponents; }

    public static class IntentFilterInfo {
        private String component;
        private String componentType;
        private List<String> actions = new ArrayList<>();
        private List<String> categories = new ArrayList<>();
        private List<String> dataSchemes = new ArrayList<>();

        public IntentFilterInfo() {}

        public String getComponent() { return component; }
        public void setComponent(String component) { this.component = component; }
        public String getComponentType() { return componentType; }
        public void setComponentType(String componentType) { this.componentType = componentType; }
        public List<String> getActions() { return actions; }
        public void setActions(List<String> actions) { this.actions = actions; }
        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }
        public List<String> getDataSchemes() { return dataSchemes; }
        public void setDataSchemes(List<String> dataSchemes) { this.dataSchemes = dataSchemes; }
    }
}
