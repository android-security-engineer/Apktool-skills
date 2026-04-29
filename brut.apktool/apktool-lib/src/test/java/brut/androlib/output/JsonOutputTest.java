package brut.androlib.output;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class JsonOutputTest {

    @Test
    public void testToJsonSimpleObject() {
        TestObject obj = new TestObject("hello", 42);
        String json = JsonOutput.toJson(obj);
        assertTrue(json.contains("\"name\": \"hello\""));
        assertTrue(json.contains("\"value\": 42"));
    }

    @Test
    public void testToJsonList() {
        List<String> list = Arrays.asList("a", "b", "c");
        String json = JsonOutput.toJson(list);
        assertTrue(json.contains("\"a\""));
        assertTrue(json.contains("\"b\""));
        assertTrue(json.contains("\"c\""));
    }

    @Test
    public void testToJsonNullField() {
        TestObject obj = new TestObject(null, 0);
        String json = JsonOutput.toJson(obj);
        // Gson by default skips null fields in pretty printing
        assertTrue(json.contains("\"value\": 0"));
    }

    @Test
    public void testToJsonEmptyString() {
        TestObject obj = new TestObject("", 0);
        String json = JsonOutput.toJson(obj);
        assertTrue(json.contains("\"name\": \"\""));
    }

    @Test
    public void testWriteToStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TestObject obj = new TestObject("stream", 1);
        JsonOutput.write(obj, baos);
        String result = baos.toString("UTF-8");
        assertTrue(result.contains("\"name\": \"stream\""));
    }

    @Test
    public void testOutputIsValidJson() {
        TestObject obj = new TestObject("test", 123);
        String json = JsonOutput.toJson(obj);
        assertTrue("Should start with {", json.trim().startsWith("{"));
        assertTrue("Should end with }", json.trim().endsWith("}"));
    }

    private static class TestObject {
        String name;
        int value;

        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
