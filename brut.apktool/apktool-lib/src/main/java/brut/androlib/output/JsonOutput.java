package brut.androlib.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class JsonOutput {
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static void write(Object obj, OutputStream out) {
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        GSON.toJson(obj, writer);
        try {
            writer.flush();
        } catch (java.io.IOException ignored) {
        }
    }
}
