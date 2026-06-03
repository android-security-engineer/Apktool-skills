package brut.apktool.ai.cli.skill;

import brut.apktool.ai.cli.CommandDispatcher;
import brut.apktool.ai.cli.Skill;
import brut.apktool.ai.cli.SkillContext;
import brut.apktool.ai.cli.SkillResult;
import brut.apktool.ai.cli.SkillStep;

import java.io.File;
import java.util.*;

public class ResourceExplorerSkill implements Skill {

    @Override
    public String name() { return "resource-explorer"; }

    @Override
    public String description() {
        return "Resource and file exploration: resources, locales, assets, native libs, file structure";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("resources", "Resource table summary", "resources", Collections.emptyMap()));
        steps.add(new SkillStep("locales", "Supported locales", "locales", Collections.emptyMap()));
        steps.add(new SkillStep("file-list", "APK file listing", "file-list", Collections.emptyMap()));
        steps.add(new SkillStep("asset-list", "Assets directory", "asset-list", Collections.emptyMap()));
        steps.add(new SkillStep("native-libs", "Native libraries", "native-libs", Collections.emptyMap()));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        CommandDispatcher dispatcher = new CommandDispatcher(context);
        String[] keys = {"resources", "locales", "fileList", "assets", "nativeLibs"};
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