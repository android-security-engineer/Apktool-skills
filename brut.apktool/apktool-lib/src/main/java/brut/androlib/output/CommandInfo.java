package brut.androlib.output;

import java.util.ArrayList;
import java.util.List;

public class CommandInfo {
    private String name;
    private String shortName;
    private String description;
    private String usage;
    private String outputFormat;
    private List<String> examples = new ArrayList<>();
    private List<ParamInfo> params = new ArrayList<>();
    private String category;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUsage() { return usage; }
    public void setUsage(String usage) { this.usage = usage; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    public List<String> getExamples() { return examples; }
    public void setExamples(List<String> examples) { this.examples = examples; }
    public List<ParamInfo> getParams() { return params; }
    public void setParams(List<ParamInfo> params) { this.params = params; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public static class ParamInfo {
        private String name;
        private String shortName;
        private String description;
        private boolean required;
        private String defaultValue;

        public ParamInfo() {}

        public ParamInfo(String name, String description, boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    }
}
