package brut.apktool.serve;

import brut.androlib.Config;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ApktoolServer {
    private final Javalin app;
    private final ApiHandler handler;

    public ApktoolServer(int port) {
        Config config = new Config("ai-apktool-serve");
        handler = new ApiHandler(config);

        app = Javalin.create();

        registerRoutes();
        app.start(port);
    }

    private void registerRoutes() {
        app.get("/api/v1/health", ctx -> ctx.result("{\"status\":\"ok\"}"));
        app.get("/api/v1/info", this::handleInfo);
        app.get("/api/v1/manifest", this::handleManifest);
        app.get("/api/v1/permissions", this::handlePermissions);
        app.get("/api/v1/activities", this::handleActivities);
        app.get("/api/v1/services", this::handleServices);
        app.get("/api/v1/receivers", this::handleReceivers);
        app.get("/api/v1/providers", this::handleProviders);
        app.get("/api/v1/components", this::handleComponents);
        app.get("/api/v1/sdk-info", this::handleSdkInfo);
        app.get("/api/v1/resources", this::handleResources);
        app.get("/api/v1/security", this::handleSecurity);
        app.get("/api/v1/api-surface", this::handleApiSurface);
        app.get("/api/v1/signing", this::handleSigning);
        app.get("/api/v1/structure", this::handleStructure);
        app.get("/api/v1/analyze", this::handleAnalyze);
        app.get("/api/v1/ai", this::handleAi);
        app.get("/api/v1/search", this::handleSearch);
        app.get("/api/v1/diff", this::handleDiff);
    }

    private void handleInfo(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleInfo(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleManifest(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleManifest(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handlePermissions(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handlePermissions(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleActivities(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "activities"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleServices(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "services"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleReceivers(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "receivers"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleProviders(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "providers"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleComponents(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleAllComponents(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSdkInfo(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleSdkInfo(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleResources(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleResources(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSecurity(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleSecurity(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleApiSurface(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleApiSurface(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSigning(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleSigning(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleStructure(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleStructure(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAnalyze(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleAnalyze(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAi(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            String action = ctx.queryParamAsClass("action", String.class).getOrDefault("explain");
            ctx.contentType("text/plain").result(handler.handleAi(apk, action));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSearch(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            String type = ctx.queryParamAsClass("type", String.class).getOrDefault("classes");
            String pattern = ctx.queryParamAsClass("pattern", String.class).getOrDefault(".*");
            ctx.contentType("application/json").result(handler.handleSearch(apk, type, pattern));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleDiff(Context ctx) {
        try {
            String apk1 = getRequiredParam(ctx, "apk1");
            String apk2 = getRequiredParam(ctx, "apk2");
            ctx.contentType("application/json").result(handler.handleDiff(apk1, apk2));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String getRequiredParam(Context ctx, String name) {
        String value = ctx.queryParam(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        return value;
    }

    public String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public void stop() {
        app.stop();
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        System.out.println("Starting AI-Apktool server on port " + port + "...");
        new ApktoolServer(port);
    }
}