package brut.apktool.ai.cli.skill;

import brut.apktool.ai.cli.CommandDispatcher;
import brut.apktool.ai.cli.Skill;
import brut.apktool.ai.cli.SkillContext;
import brut.apktool.ai.cli.SkillResult;
import brut.apktool.ai.cli.SkillStep;

import java.io.File;
import java.util.*;

public class NetworkAnalysisSkill implements Skill {

    @Override
    public String name() { return "network-analysis"; }

    @Override
    public String description() {
        return "Network communication analysis: endpoints, URLs, cleartext traffic, network security";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("manifest-flags", "Check cleartext traffic flag", "manifest-flags", Collections.emptyMap()));
        steps.add(new SkillStep("search-urls", "Search for URL patterns", "search",
                Map.of("type", "strings", "pattern", "https?://[\\w./\\-?=&%]+")));
        steps.add(new SkillStep("search-net-classes", "Search for network classes", "search",
                Map.of("type", "classes", "pattern", "(OkHttp|Retrofit|HttpURLConnection|Volley)")));
        steps.add(new SkillStep("api-surface", "Exported components with intent filters", "api-surface", Collections.emptyMap()));
        steps.add(new SkillStep("security", "Full security report", "security", Collections.emptyMap()));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        CommandDispatcher dispatcher = new CommandDispatcher(context);
        String[] keys = {"manifestFlags", "urls", "netClasses", "apiSurface", "security"};
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