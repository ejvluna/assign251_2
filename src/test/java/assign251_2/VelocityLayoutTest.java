package assign251_2;

// Import the necessary classes
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Class to test the VelocityLayout class
public class VelocityLayoutTest {

    // variable to store the VelocityLayout instance
    private VelocityLayout layout;

    // Method to set up the test environment
    @BeforeEach
    public void setUp() {
        layout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "[$p] $c $d: $m$n");
    }

    // REQUIRED test case to test the setPattern method of the VelocityLayout class
    @Test
    public void testSetPattern() {
        layout.setPattern("$t - $m");
        LogEvent event = Log4jLogEvent.newBuilder()
                .setThreadName("TestThread")
                .setMessage(new SimpleMessage("Test message"))
                .build();
        String result = layout.toSerializable(event);
        assertTrue(result.matches("TestThread - Test message"));
    }

    // Method to test the layout with different log levels
    @Test
    public void testDifferentLogLevels() {
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.ERROR)
                .setMessage(new SimpleMessage("Error message"))
                .setThreadName("TestThread")
                .setTimeMillis(new Date().getTime())
                .build();

                String result = layout.toSerializable(event);
                System.out.println(result); // Print the result for debugging
        
                // Adjust the regex to match the date format
                assertTrue(result.matches("\\[ERROR\\] TestLogger .+: Error message" + System.lineSeparator()));
                assertTrue(result.contains("TestLogger"));
                assertTrue(result.contains("Error message"));
                assertTrue(result.endsWith(System.lineSeparator()));
    }

    // Method to test the toSerializable method of the VelocityLayout class
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
}
