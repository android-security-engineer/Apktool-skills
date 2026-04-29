package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class ComponentInfo {
    private String name;
    private String type;
    private boolean exported;
    private List<String> intentFilters = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    public ComponentInfo() {}

    public ComponentInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isExported() { return exported; }
    public void setExported(boolean exported) { this.exported = exported; }
    public List<String> getIntentFilters() { return intentFilters; }
    public void setIntentFilters(List<String> intentFilters) { this.intentFilters = intentFilters; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
