package brut.apktool.ai.cli.skill;

import brut.androlib.analyze.ApkAnalyzer;
import brut.apktool.ai.cli.Skill;
import brut.apktool.ai.cli.SkillContext;
import brut.apktool.ai.cli.SkillResult;
import brut.apktool.ai.cli.SkillStep;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickAnalysisSkill implements Skill {

    @Override
    public String name() {
        return "quick-analysis";
    }

    @Override
    public String description() {
        return "Quick APK assessment: overview, security, attack surface, and risk interpretation";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("overview", "Get APK metadata overview", "info", Collections.emptyMap()));
        steps.add(new SkillStep("security", "Get security report with risk score", "security", Collections.emptyMap()));
        steps.add(new SkillStep("api-surface", "Get exported components attack surface", "api-surface", Collections.emptyMap()));
        steps.add(new SkillStep("interpret", "Interpret risk score and flag issues", "interpret", Collections.emptyMap()));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        try {
            ApkAnalyzer analyzer = context.getAnalyzer();

            SkillStep step1 = steps().get(0);
            step1.setStatus(SkillStep.StepStatus.RUNNING);
            Object summary = analyzer.getSummary();
            step1.setResult(summary);
            step1.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("info", summary);
            result.addStep(step1);

            SkillStep step2 = steps().get(1);
            step2.setStatus(SkillStep.StepStatus.RUNNING);
            Object securityReport = analyzer.getSecurityReport();
            step2.setResult(securityReport);
            step2.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("security", securityReport);
            result.addStep(step2);

            SkillStep step3 = steps().get(2);
            step3.setStatus(SkillStep.StepStatus.RUNNING);
            Object apiSurface = analyzer.getApiSurface();
            step3.setResult(apiSurface);
            step3.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("apiSurface", apiSurface);
            result.addStep(step3);

            SkillStep step4 = steps().get(3);
            step4.setStatus(SkillStep.StepStatus.RUNNING);
            Object permissionDetail = analyzer.getPermissionDetail();
            step4.setResult(permissionDetail);
            step4.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("riskInterpretation", permissionDetail);
            result.addStep(step4);

        } catch (Exception e) {
            result.setSuccess(false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}