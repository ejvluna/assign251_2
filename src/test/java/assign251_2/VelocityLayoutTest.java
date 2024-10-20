package assign251_2;

// Import the necessary classes
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Class to test the VelocityLayout class
public class VelocityLayoutTest {

    // === Section 0. Test environment setup

    // variable to store the VelocityLayout instance
    private VelocityLayout layout;

    // Set up the test environment before each test
    @BeforeEach
    public void setUp() {
        layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, null);
    }

    // === Section 1. Test layout creation

    // Test the createLayout method with the default pattern
    @Test
    public void testCreateLayoutWithDefaultPattern() {
        assertNotNull(layout, "Layout should not be null");
        String defaultPattern = layout.getPattern();
        assertNotNull(defaultPattern, "Default pattern should not be null");
        assertEquals("[$p] $c $d: $m$n", defaultPattern, "Default pattern should match");
    }

    // Test the createLayout method with a custom pattern
    @Test
    public void testCreateLayoutWithCustomPattern() {
        String customPattern = "$m - $c [$p]";
        VelocityLayout customLayout = VelocityLayout.createLayout(StandardCharsets.UTF_8, customPattern);
        assertNotNull(customLayout, "Layout should not be null");
        assertEquals(customPattern, customLayout.getPattern(), "Custom pattern should match");
    }


    // === Section 2. Test log event formatting

    // Test formatting of log messages with all variables
    @Test
    public void testFormattingAllVariables() {
        // Create a custom pattern that includes all variables
        String customPattern = "[$p] $c ($t) $d - $m$n";
        VelocityLayout customLayout = VelocityLayout.createLayout(StandardCharsets.UTF_8, customPattern);

        // Create a log event with all variables set
        long timestamp = System.currentTimeMillis();
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message"))
                .setThreadName("TestThread")
                .setTimeMillis(timestamp)
                .build();

        // Format the log event
        String result = customLayout.toSerializable(event);

        // Check if all variables are present in the result
        assertTrue(result.contains("[INFO]"), "Log level should be present");
        assertTrue(result.contains("TestLogger"), "Logger name should be present");
        assertTrue(result.contains("TestThread"), "Thread name should be present");
        assertTrue(result.contains(new Date(timestamp).toString()), "Date should be present");
        assertTrue(result.contains("Test message"), "Message should be present");
        assertTrue(result.endsWith(System.lineSeparator()), "Should end with line separator");
    }

    // Test formatting of log messages for all log levels
    @Test
    public void testDifferentLogLevels() {
        Level[] levels = {Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};
        
        for (Level level : levels) {
            LogEvent event = Log4jLogEvent.newBuilder()
                    .setLoggerName("TestLogger")
                    .setLevel(level)
                    .setMessage(new SimpleMessage("Test message"))
                    .setThreadName("TestThread")
                    .setTimeMillis(System.currentTimeMillis())
                    .build();

            String result = layout.toSerializable(event);

            assertTrue(result.contains("[" + level.toString() + "]"), "Log level " + level + " should be present");
            assertTrue(result.contains("TestLogger"), "Logger name should be present for level " + level);
            assertTrue(result.contains("Test message"), "Message should be present for level " + level);
        }
    }


    // Test formatting of log messages with variables missing ????
    @Test
    public void testVariableSupport() {
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message"))
                .setThreadName("TestThread")
                .setTimeMillis(new Date().getTime())
                .build();

        String result = layout.toSerializable(event);
        System.out.println(result); // Print the result for debugging

        // Adjust the regex to match the date format
        assertTrue(result.matches("\\[INFO\\] TestLogger .+: Test message" + System.lineSeparator()));
        assertTrue(result.contains("TestLogger"));
        assertTrue(result.contains("Test message"));
        assertTrue(result.endsWith(System.lineSeparator()));
    }

    // === Section 3. Test pattern manipulation

    @Test
    public void testGetPattern() {
        // Test default pattern
        String defaultPattern = layout.getPattern();
        assertEquals("[$p] $c $d: $m$n", defaultPattern, "Default pattern should match expected value");

        // Test getting pattern after setting a new one
        String newPattern = "$m - [$p] $c";
        layout.setPattern(newPattern);
        assertEquals(newPattern, layout.getPattern(), "getPattern() should return the newly set pattern");
    }

    // Test setting a new pattern
    @Test
    public void testSetPattern() {
        // Set a new pattern
        String newPattern = "$m - [$p] $c";
        layout.setPattern(newPattern);

        // Verify that the new pattern is set correctly
        assertEquals(newPattern, layout.getPattern(), "The set pattern should match the new pattern");
    }

    // Test that a pattern is correctly used in formatting
    @Test
    public void testPatternUsedInFormatting() {
        // Set a specific pattern for this test
        String testPattern = "$m - [$p] $c";
        layout.setPattern(testPattern);

        // Create a log event
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message"))
                .build();

        // Test that the pattern is used correctly in formatting
        String formattedMessage = layout.toSerializable(event);
        assertEquals("Test message - [INFO] TestLogger", formattedMessage, 
                    "Formatted message should use the set pattern");
    }


    // === Section 4. Test edge cases

    // Test that null or empty patterns are handled correctly
    @Test
    public void testNullOrEmptyPattern() {
        // Test with null pattern
        layout.setPattern(null);
        assertEquals("[$p] $c $d: $m$n", layout.getPattern(), "Null pattern should revert to default");

        // Test with empty pattern
        layout.setPattern("");
        assertEquals("", layout.getPattern(), "Empty pattern should be allowed");

        // Verify that an empty pattern produces an empty string when formatting
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message"))
                .build();
        String formattedMessage = layout.toSerializable(event);
        assertEquals("", formattedMessage, "Empty pattern should produce empty string");
    }

    // Test that invalid variables are left as-is in the output
    @Test
    public void testInvalidVariablesInPattern() {
        // Set a pattern with invalid variables
        layout.setPattern("$invalid $x $y $z $m");

        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message"))
                .build();

        String formattedMessage = layout.toSerializable(event);

        // Check that invalid variables are left as-is in the output
        assertTrue(formattedMessage.contains("$invalid"), "Invalid variable should remain in output");
        assertTrue(formattedMessage.contains("$x"), "Invalid variable should remain in output");
        assertTrue(formattedMessage.contains("$y"), "Invalid variable should remain in output");
        assertTrue(formattedMessage.contains("$z"), "Invalid variable should remain in output");
        
        // Check that valid variables are still processed
        assertTrue(formattedMessage.contains("Test message"), "Valid variables should be processed");
    }

    // === Section 5. Test integration with MemAppender and Log4j

    // Test integration with Log4j Logger
    @Test
    public void testIntegrationWithLog4jLogger() {
        // Create a VelocityLayout
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "[$p] $c: $m$n");

        // Create a MemAppender with the VelocityLayout
        MemAppender memAppender = new MemAppender("TestMemAppender", null, layout, null);
        memAppender.start();

        // Get a Logger and add the MemAppender to it
        LoggerContext context = LoggerContext.getContext(false);
        Logger logger = context.getLogger(VelocityLayoutTest.class.getName());

        logger.addAppender(memAppender);

        // Log a message
        logger.info("Test message");

        // Verify the logged message
        List<String> logs = memAppender.getEventStrings();
        assertEquals(1, logs.size(), "Should have one log message");
        assertTrue(logs.get(0).matches("\\[INFO\\] " + VelocityLayoutTest.class.getName() + ": Test message" + System.lineSeparator()),
                "Log message should match expected format");

        // Clean up
        logger.removeAppender(memAppender);
        memAppender.stop();
    }

    // Test integration with MemAppender 
    @Test
    public void testIntegrationWithMemAppender() {
        // Create a VelocityLayout
        VelocityLayout layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "[$p] $m");

        // Create a MemAppender with the VelocityLayout
        MemAppender memAppender = new MemAppender("TestMemAppender", null, layout, null);
        memAppender.start();

        // Create a LogEvent
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message"))
                .build();

        // Append the LogEvent
        memAppender.append(event);

        // Verify the logged message
        List<String> logs = memAppender.getEventStrings();
        assertEquals(1, logs.size(), "Should have one log message");
        assertEquals("[INFO] Test message", logs.get(0), "Log message should match expected format");

        // Clean up
        memAppender.stop();
    }


}
