package brut.apktool.ai.cli;

import java.io.File;
import java.util.List;

public interface Skill {

    String name();

    String description();

    List<SkillStep> steps();

    SkillResult execute(File apkFile, SkillContext context);
}