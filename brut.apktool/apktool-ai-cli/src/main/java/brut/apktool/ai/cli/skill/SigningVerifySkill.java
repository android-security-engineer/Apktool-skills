package brut.apktool.ai.cli.skill;

import brut.apktool.ai.cli.CommandDispatcher;
import brut.apktool.ai.cli.Skill;
import brut.apktool.ai.cli.SkillContext;
import brut.apktool.ai.cli.SkillResult;
import brut.apktool.ai.cli.SkillStep;

import java.io.File;
import java.util.*;

public class SigningVerifySkill implements Skill {

    @Override
    public String name() { return "signing-verify"; }

    @Override
    public String description() {
        return "APK signing verification: certificate details, signing schemes, security assessment";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("signing", "Get signing certificate info", "signing", Collections.emptyMap()));
        steps.add(new SkillStep("manifest-flags", "Check debuggable flag", "manifest-flags", Collections.emptyMap()));
        steps.add(new SkillStep("file-hash", "Get APK file hashes", "file-hash", Collections.emptyMap()));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        CommandDispatcher dispatcher = new CommandDispatcher(context);
        String[] keys = {"signing", "manifestFlags", "fileHash"};
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