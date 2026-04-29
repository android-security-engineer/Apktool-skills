package brut.androlib.output;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CommandRegistryTest {

    @Test
    public void testGetAllCommandsReturnsNonEmpty() {
        List<CommandInfo> commands = CommandRegistry.getAllCommands();
        assertFalse("Should have registered commands", commands.isEmpty());
        assertTrue("Should have at least 20 commands", commands.size() >= 20);
    }

    @Test
    public void testGetCommandByName() {
        CommandInfo cmd = CommandRegistry.getCommand("info");
        assertNotNull("'info' command should exist", cmd);
        assertEquals("info", cmd.getName());
        assertNotNull("Should have description", cmd.getDescription());
        assertNotNull("Should have output format", cmd.getOutputFormat());
        assertNotNull("Should have examples", cmd.getExamples());
        assertFalse("Should have at least 1 example", cmd.getExamples().isEmpty());
    }

    @Test
    public void testGetCommandWithShortName() {
        CommandInfo decode = CommandRegistry.getCommand("decode");
        assertNotNull(decode);
        assertEquals("d", decode.getShortName());
    }

    @Test
    public void testGetCommandsByCategory() {
        List<CommandInfo> core = CommandRegistry.getCommandsByCategory("core");
        assertFalse("Core category should have commands", core.isEmpty());

        List<CommandInfo> analysis = CommandRegistry.getCommandsByCategory("analysis");
        assertFalse("Analysis category should have commands", analysis.isEmpty());
    }

    @Test
    public void testToJsonCatalogProducesValidJson() {
        String json = CommandRegistry.toJsonCatalog();
        assertNotNull(json);
        assertTrue("Should contain tool name", json.contains("AI-Apktool"));
        assertTrue("Should contain commands", json.contains("commands"));
        assertTrue("Should contain info command", json.contains("info"));
        assertTrue("Should contain outputFormat", json.contains("outputFormat"));
    }

    @Test
    public void testAllAnalysisCommandsHaveOutputFormat() {
        List<CommandInfo> analysis = CommandRegistry.getCommandsByCategory("analysis");
        for (CommandInfo cmd : analysis) {
            assertNotNull(cmd.getName() + " should have outputFormat", cmd.getOutputFormat());
            assertTrue(cmd.getName() + " outputFormat should mention JSON", cmd.getOutputFormat().contains("JSON"));
        }
    }

    @Test
    public void testAllCommandsHaveExamples() {
        List<CommandInfo> commands = CommandRegistry.getAllCommands();
        for (CommandInfo cmd : commands) {
            assertFalse(cmd.getName() + " should have examples", cmd.getExamples().isEmpty());
        }
    }
}
