package brut.apktool.ai.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillRegistry {

    private final Map<String, Skill> skills = new LinkedHashMap<>();

    public void register(Skill skill) {
        skills.put(skill.name(), skill);
    }

    public Skill get(String name) {
        return skills.get(name);
    }

    public List<Skill> all() {
        return Collections.unmodifiableList(new ArrayList<>(skills.values()));
    }

    public List<String> names() {
        return new ArrayList<>(skills.keySet());
    }

    public boolean hasSkill(String name) {
        return skills.containsKey(name);
    }

    public static SkillRegistry createDefault() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new brut.apktool.ai.cli.skill.QuickAnalysisSkill());
        registry.register(new brut.apktool.ai.cli.skill.SecurityAuditSkill());
        return registry;
    }
}