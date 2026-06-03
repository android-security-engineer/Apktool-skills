package brut.apktool.ai.cli;

import java.util.Map;

public class SkillStep {

    private final String name;
    private final String description;
    private final String command;
    private final Map<String, String> params;
    private StepStatus status;
    private Object result;

    public enum StepStatus {
        PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    }

    public SkillStep(String name, String description, String command, Map<String, String> params) {
        this.name = name;
        this.description = description;
        this.command = command;
        this.params = params;
        this.status = StepStatus.PENDING;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCommand() { return command; }
    public Map<String, String> getParams() { return params; }
    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
}