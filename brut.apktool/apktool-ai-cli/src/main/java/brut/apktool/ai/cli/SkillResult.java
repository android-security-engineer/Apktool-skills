package brut.apktool.ai.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillResult {

    private final String skillName;
    private final Map<String, Object> data;
    private final List<SkillStep> steps;
    private boolean success;

    public SkillResult(String skillName) {
        this.skillName = skillName;
        this.data = new LinkedHashMap<>();
        this.steps = new ArrayList<>();
        this.success = true;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void addStep(SkillStep step) {
        steps.add(step);
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSkillName() { return skillName; }
    public Map<String, Object> getData() { return data; }
    public List<SkillStep> getSteps() { return steps; }
    public boolean isSuccess() { return success; }
}