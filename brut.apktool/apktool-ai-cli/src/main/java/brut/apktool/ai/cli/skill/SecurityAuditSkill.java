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

public class SecurityAuditSkill implements Skill {

    @Override
    public String name() {
        return "security-audit";
    }

    @Override
    public String description() {
        return "Comprehensive security audit: OWASP-mapped findings, risk score, and remediation";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("security-report", "Automated security report", "security", Collections.emptyMap()));
        steps.add(new SkillStep("api-surface", "Attack surface analysis", "api-surface", Collections.emptyMap()));
        steps.add(new SkillStep("permissions", "Permission deep dive", "permissions", Collections.emptyMap()));
        steps.add(new SkillStep("manifest-flags", "Manifest security flags", "manifest-flags", Collections.emptyMap()));
        steps.add(new SkillStep("signing", "Signing certificate review", "signing", Collections.emptyMap()));
        steps.add(new SkillStep("sensitive-data", "Search for hardcoded secrets", "search", Collections.singletonMap("type", "strings")));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        try {
            ApkAnalyzer analyzer = context.getAnalyzer();

            SkillStep step1 = steps().get(0);
            step1.setStatus(SkillStep.StepStatus.RUNNING);
            Object securityReport = analyzer.getSecurityReport();
            step1.setResult(securityReport);
            step1.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("security", securityReport);
            result.addStep(step1);

            SkillStep step2 = steps().get(1);
            step2.setStatus(SkillStep.StepStatus.RUNNING);
            Object apiSurface = analyzer.getApiSurface();
            step2.setResult(apiSurface);
            step2.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("apiSurface", apiSurface);
            result.addStep(step2);

            SkillStep step3 = steps().get(2);
            step3.setStatus(SkillStep.StepStatus.RUNNING);
            Object permissionDetail = analyzer.getPermissionDetail();
            step3.setResult(permissionDetail);
            step3.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("permissions", permissionDetail);
            result.addStep(step3);

            SkillStep step4 = steps().get(3);
            step4.setStatus(SkillStep.StepStatus.RUNNING);
            Object manifestFlags = analyzer.getManifestInfo();
            step4.setResult(manifestFlags);
            step4.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("manifestFlags", manifestFlags);
            result.addStep(step4);

            SkillStep step5 = steps().get(4);
            step5.setStatus(SkillStep.StepStatus.RUNNING);
            Object signing = analyzer.getSigningInfo();
            step5.setResult(signing);
            step5.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("signing", signing);
            result.addStep(step5);

            SkillStep step6 = steps().get(5);
            step6.setStatus(SkillStep.StepStatus.RUNNING);
            Object searchResults = context.getSearcher().searchStrings("password|secret|token|api.?key");
            step6.setResult(searchResults);
            step6.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("sensitiveData", searchResults);
            result.addStep(step6);

        } catch (Exception e) {
            result.setSuccess(false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}