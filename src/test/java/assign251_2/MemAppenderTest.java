package assign251_2; // Specify the package of the class

// Import the necessary classes 
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.core.layout.PatternLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintStream;
import java.util.List;

// Class to test the MemAppender class
public class MemAppenderTest {

    private MemAppender memAppender; // variable to store the MemAppender instance
    private static final Logger logger = LogManager.getLogger(MemAppenderTest.class);

    

    // Method to set up the test environment
    @BeforeEach
    public void setUp() {
        // use the injected constructor to set the logEvents list
        memAppender = new MemAppender("TestMemAppender", null, PatternLayout.createDefaultLayout(), null);
    }
    // Method to test the append and getCurrentLogs methods of the MemAppender class
    @Test
    public void testAppendAndGetCurrentLogs() {
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message"))
                .build();
        memAppender.append(event);

        assertEquals(1, memAppender.getCurrentLogs().size(), "Should have one log event");
        assertEquals("Test message", memAppender.getCurrentLogs().get(0).getMessage().getFormattedMessage());
    }

    // Method to verify that the MemAppender class follows the Singleton pattern
    @Test
    public void testSingletonPattern() {
        // Use the injected constructor to create the MemAppender instances
        MemAppender appender1 = MemAppender.createAppender("TestAppender1", PatternLayout.createDefaultLayout(), null);
        MemAppender appender2 = MemAppender.createAppender("TestAppender2", PatternLayout.createDefaultLayout(), null);
        assertSame(appender1, appender2, "MemAppender should be a singleton");
    }    

    // Method to test the setMaxSize and getDiscardedLogCount methods of the MemAppender class
    @Test
    public void testMaxSizeAndLogRotation() {
        // Use dependency injection constructor to create the MemAppender instance
        MemAppender appender = new MemAppender("TestAppender", null, PatternLayout.createDefaultLayout(), null);
        appender.setMaxSize(3);
        
        for (int i = 0; i < 5; i++) {
            appender.append(Log4jLogEvent.newBuilder()
                .setMessage(new SimpleMessage("Message " + i))
                .build());
        }
        
        List<LogEvent> logs = appender.getCurrentLogs();
        assertEquals(3, logs.size(), "Should only keep 3 logs due to maxSize");
        assertEquals("Message 2", logs.get(0).getMessage().getFormattedMessage());
        assertEquals("Message 4", logs.get(2).getMessage().getFormattedMessage());
        assertEquals(2, appender.getDiscardedLogCount(), "Should have discarded 2 logs");
    }

    // Method to test the getEventStrings method of the MemAppender class
    @Test
    public void testGetEventStrings() {
        // Use dependency injection constructor to create the MemAppender instance
        MemAppender appender = new MemAppender("TestAppender", null, PatternLayout.createDefaultLayout(), null);
        appender.start(); // Make sure to start the appender

        LogEvent event = Log4jLogEvent.newBuilder()
            .setLevel(Level.INFO)
            .setMessage(new SimpleMessage("Test message"))
            .build();
        appender.append(event);
        
        List<String> eventStrings = appender.getEventStrings();
        assertFalse(eventStrings.isEmpty(), "Event strings list should not be empty");
        assertTrue(eventStrings.get(0).contains("Test message"), "Event string should contain the test message");
    }

    // Method to test the printLogs method of the MemAppender class
    @Test
    public void testPrintLogs() {
        // Use dependency injection constructor to create the MemAppender instance
        MemAppender appender = new MemAppender("TestAppender", null, PatternLayout.createDefaultLayout(), null);
        appender.start(); // Make sure to start the appender

        LogEvent event = Log4jLogEvent.newBuilder()
            .setLevel(Level.INFO)
            .setMessage(new SimpleMessage("Test message"))
            .build();
        appender.append(event);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        appender.printLogs();
        
        System.setOut(originalOut); // Restore the original System.out
        
        assertTrue(outContent.toString().contains("Test message"), "Printed logs should contain the test message");
        assertTrue(appender.getCurrentLogs().isEmpty(), "Logs should be cleared after printing");
    }


}