package brut.androlib.analyze;

import brut.androlib.output.JsonOutput;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ApkDiffTest {

    @Test
    public void testFindAddedLogic() {
        Set<String> old = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> now = new HashSet<>(Arrays.asList("b", "c", "d"));
        List<String> added = new java.util.ArrayList<>();
        for (String item : now) {
            if (!old.contains(item)) added.add(item);
        }
        assertEquals(1, added.size());
        assertTrue(added.contains("d"));
    }

    @Test
    public void testFindRemovedLogic() {
        Set<String> old = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> now = new HashSet<>(Arrays.asList("b", "c", "d"));
        List<String> removed = new java.util.ArrayList<>();
        for (String item : old) {
            if (!now.contains(item)) removed.add(item);
        }
        assertEquals(1, removed.size());
        assertTrue(removed.contains("a"));
    }

    @Test
    public void testDiffResultJsonOutput() {
        DiffResult result = new DiffResult();
        result.getAddedPermissions().add("android.permission.CAMERA");
        result.setVersionCodeChange("1 -> 2");
        String json = JsonOutput.toJson(result);
        assertNotNull(json);
        assertTrue(json.contains("addedPermissions"));
        assertTrue(json.contains("CAMERA"));
        assertTrue(json.contains("versionCodeChange"));
    }

    @Test
    public void testStructureInfoJsonOutput() {
        StructureInfo info = new StructureInfo();
        info.setTotalClasses(100);
        info.setTotalMethods(500);
        info.setDexCount(2);
        String json = JsonOutput.toJson(info);
        assertNotNull(json);
        assertTrue(json.contains("totalClasses"));
        assertTrue(json.contains("totalMethods"));
    }
}
