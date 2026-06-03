package brut.apktool.ai.cli;

import brut.androlib.Config;
import brut.androlib.output.JsonOutput;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class Main {

    private static final Properties props = new Properties();
    private static final Config config;

    static {
        try {
            props.load(Main.class.getResourceAsStream("/apktool.properties"));
        } catch (Exception ignored) {}
        config = new Config(props.getProperty("version", "unknown"));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String command = args[0];

        switch (command) {
            case "skill":
                cmdSkill(args);
                break;
            case "list":
            case "ls":
                cmdListSkills();
                break;
            case "help":
            case "h":
                printUsage();
                break;
            case "version":
            case "v":
                System.out.println("apktool-ai-cli " + props.getProperty("version", "unknown"));
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }

    private static void cmdSkill(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: apktool-ai-cli skill <skill-name> <apk-file>");
            System.exit(1);
        }

        String skillName = args[1];
        String apkPath = args[2];

        SkillRegistry registry = SkillRegistry.createDefault();

        if (!registry.hasSkill(skillName)) {
            System.err.println("Unknown skill: " + skillName);
            System.err.println("Available skills: " + String.join(", ", registry.names()));
            System.exit(1);
        }

        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            System.err.println("APK file not found: " + apkPath);
            System.exit(1);
        }

        SkillContext context = new SkillContext(apkFile, config);
        Skill skill = registry.get(skillName);

        System.err.println("Running skill: " + skill.name() + " - " + skill.description());
        SkillResult result = skill.execute(apkFile, context);

        System.out.println(JsonOutput.toJson(result));
    }

    private static void cmdListSkills() {
        SkillRegistry registry = SkillRegistry.createDefault();
        List<Skill> skills = registry.all();

        System.out.println("{");
        System.out.println("  \"totalSkills\": " + skills.size() + ",");
        System.out.println("  \"skills\": [");
        for (int i = 0; i < skills.size(); i++) {
            Skill s = skills.get(i);
            System.out.println("    {\"name\": \"" + s.name() + "\", " +
                "\"description\": \"" + s.description() + "\", " +
                "\"steps\": " + s.steps().size() + "}" +
                (i < skills.size() - 1 ? "," : ""));
        }
        System.out.println("  ]");
        System.out.println("}");
    }

    private static void printUsage() {
        System.out.println("apktool-ai-cli - AI-native Android reverse engineering skill executor");
        System.out.println();
        System.out.println("Usage: apktool-ai-cli <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  skill <name> <apk>  Execute a named skill on an APK file");
        System.out.println("  list                List all available skills");
        System.out.println("  help                Show this help message");
        System.out.println("  version             Show version");
    }
}