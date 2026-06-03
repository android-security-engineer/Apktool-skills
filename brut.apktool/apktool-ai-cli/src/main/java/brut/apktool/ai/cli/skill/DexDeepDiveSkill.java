package brut.apktool.ai.cli.skill;

import brut.apktool.ai.cli.CommandDispatcher;
import brut.apktool.ai.cli.Skill;
import brut.apktool.ai.cli.SkillContext;
import brut.apktool.ai.cli.SkillResult;
import brut.apktool.ai.cli.SkillStep;

import java.io.File;
import java.util.*;

public class DexDeepDiveSkill implements Skill {

    @Override
    public String name() { return "dex-deep-dive"; }

    @Override
    public String description() {
        return "Deep DEX analysis: class/method/field exploration, inheritance tracing, string extraction";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("dex-overview", "DEX file list and stats", "dex-list", Collections.emptyMap()));
        steps.add(new SkillStep("dex-info", "Per-DEX statistics", "dex-info", Collections.emptyMap()));
        steps.add(new SkillStep("class-list", "All class names", "class-list", Collections.emptyMap()));
        steps.add(new SkillStep("dex-strings", "Extract DEX strings", "dex-strings", Collections.emptyMap()));
        steps.add(new SkillStep("search-methods", "Search method signatures", "method-search", Collections.singletonMap("pattern", ".*")));
        steps.add(new SkillStep("search-fields", "Search field names", "field-search", Collections.singletonMap("pattern", ".*")));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        CommandDispatcher dispatcher = new CommandDispatcher(context);
        String[] keys = {"dexList", "dexInfo", "classList", "dexStrings", "methods", "fields"};
        try {
            for (int i = 0; i < steps().size(); i++) {
                SkillStep step = steps().get(i);
                step.setStatus(SkillStep.StepStatus.RUNNING);
                Object output = dispatcher.dispatch(step.getCommand(), step.getParams());
                step.setResult(output);
                step.setStatus(SkillStep.StepStatus.COMPLETED);
                result.put(keys[i], output);
                result.addStep(step);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}