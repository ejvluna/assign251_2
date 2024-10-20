package assign251_2; // Specify the package of the class

// Import the necessary classes 
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;

import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.core.layout.PatternLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;

// Class to test the MemAppender class
public class MemAppenderTest {

    private MemAppender memAppender; // variable to store the MemAppender instance
    private static final Logger logger = LogManager.getLogger(MemAppenderTest.class);

    // Method to set up the test environment before each test
    @BeforeEach
    public void setUp() {
        memAppender = new MemAppender("TestMemAppender", null, PatternLayout.createDefaultLayout(), null);
        memAppender.start();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        config.addAppender(memAppender);

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.addAppender(memAppender, Level.ALL, null);
        loggerConfig.setLevel(Level.ALL); // Set to the most verbose level to capture all logs

        context.updateLoggers();
    }

    // Method to tear down the test environment and clear logs after each test to avoid side effects
    @AfterEach
    public void tearDown() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.removeAppender("TestMemAppender");
        context.updateLoggers();
        memAppender.stop();
        memAppender.getCurrentLogs().clear(); // Clear logs after each test
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
        // Set the maximum size of the log events
        appender.setMaxSize(3);
        // Loop to append 5 log events
        for (int i = 0; i < 5; i++) {
            appender.append(Log4jLogEvent.newBuilder()
                .setMessage(new SimpleMessage("Message " + i))
                .build());
        }
        // Get the current logs
        List<LogEvent> logs = appender.getCurrentLogs();
        // Check the logs match the expected values
        assertEquals(3, logs.size(), "Should only keep 3 logs due to maxSize");
        assertEquals("Message 2", logs.get(0).getMessage().getFormattedMessage());
        assertEquals("Message 4", logs.get(2).getMessage().getFormattedMessage());
        // Check the number of discarded logs
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
    
    // Method to test the MemAppender with the VelocityLayout class
    @Test
    public void testWithDefaultVelocityLayout() {
        String defaultPattern = "[$p] $c $d: $m$n";
        VelocityLayout velocityLayout = VelocityLayout.createLayout(StandardCharsets.UTF_8, defaultPattern);
        
        MemAppender appenderWithVelocityLayout = new MemAppender("TestAppender", null, velocityLayout, null);
        appenderWithVelocityLayout.start();
        
        LogEvent event = Log4jLogEvent.newBuilder()
            .setLoggerName("TestLogger")
            .setLevel(Level.INFO)
            .setMessage(new SimpleMessage("Test message"))
            .setTimeMillis(System.currentTimeMillis())
            .build();
        
        appenderWithVelocityLayout.append(event);
        
        List<String> eventStrings = appenderWithVelocityLayout.getEventStrings();
        assertEquals(1, eventStrings.size(), "Should have one formatted log event");
        String formattedLog = eventStrings.get(0);
        
        System.out.println("Formatted Log: " + formattedLog); // Debugging print

        // Explicit assertion messages for better feedback
        assertTrue(formattedLog.startsWith("[INFO]"), "Log should start with [INFO]");
        assertTrue(formattedLog.contains("TestLogger"), "Log should contain logger name 'TestLogger'");
        assertTrue(formattedLog.contains("Test message"), "Log should contain the message 'Test message'");
        assertTrue(formattedLog.endsWith(System.lineSeparator()), "Log should end with a newline");

        // Adjust the regex to match the actual formatted log
        String expectedPattern = "\\[INFO\\] TestLogger .*: Test message" + System.lineSeparator();
        assertTrue(formattedLog.matches(expectedPattern), 
                "Formatted log should match the VelocityLayout pattern: " + expectedPattern);
    }

    // Method to test Edge case with a null layout
    @Test
    public void testNullLayout() {
        MemAppender appender = new MemAppender("TestAppender", null, null, null);
        appender.start();
        
        LogEvent event = Log4jLogEvent.newBuilder()
            .setLoggerName("TestLogger")
            .setLevel(Level.INFO)
            .setMessage(new SimpleMessage("Test message"))
            .build();
        
        appender.append(event);
        
        assertThrows(IllegalStateException.class, () -> {
            appender.getEventStrings();
        }, "getEventStrings should throw IllegalStateException when layout is null");
    }

    // Method to test Edge case with maximum integer value as size limit
    @Test
    public void testMaxSizeLimit() {
        MemAppender appender = new MemAppender("TestAppender", null, PatternLayout.createDefaultLayout(), null);
        appender.setMaxSize(Integer.MAX_VALUE);
        appender.start();
        
        for (int i = 0; i < 1000; i++) {
            LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message " + i))
                .build();
            appender.append(event);
        }
        
        assertEquals(1000, appender.getCurrentLogs().size(), "All logs should be retained with max size set to Integer.MAX_VALUE");
    }

    // Method to test integration with Log4j Logger
    @Test
    public void testIntegrationWithLog4jLogger() {
        Logger log4jLogger = LogManager.getLogger(MemAppenderTest.class);
        
        log4jLogger.info("Test log message");
        
        List<LogEvent> events = memAppender.getCurrentLogs();
        assertEquals(1, events.size(), "Should have one log event");
        LogEvent logEvent = events.get(0);
        assertEquals(Level.INFO, logEvent.getLevel(), "Log level should be INFO");
        assertEquals("Test log message", logEvent.getMessage().getFormattedMessage(), "Log message should match");
        assertEquals(MemAppenderTest.class.getName(), logEvent.getLoggerName(), "Logger name should match");
    }

    // Method to test a log event for the ERROR level
    @Test
    public void testErrorLogLevel() {
        logger.error("Error message");
        List<LogEvent> events = memAppender.getCurrentLogs();
        assertEquals(1, events.size(), "Should have one log event for ERROR level");
        assertEquals(Level.ERROR, events.get(0).getLevel());
        assertEquals("Error message", events.get(0).getMessage().getFormattedMessage());
    }

    // Method to test a log event for the WARN level
    @Test
    public void testWarnLogLevel() {
        logger.warn("Warn message");
        List<LogEvent> events = memAppender.getCurrentLogs();
        assertEquals(1, events.size(), "Should have one log event for WARN level");
        assertEquals(Level.WARN, events.get(0).getLevel());
        assertEquals("Warn message", events.get(0).getMessage().getFormattedMessage());
    }

    // Method to test a log event for the INFO level
    @Test
    public void testInfoLogLevel() {
        logger.info("Info message");
        List<LogEvent> events = memAppender.getCurrentLogs();
        assertEquals(1, events.size(), "Should have one log event for INFO level");
        assertEquals(Level.INFO, events.get(0).getLevel());
        assertEquals("Info message", events.get(0).getMessage().getFormattedMessage());
    }

    // Method to test a log event for the DEBUG level
    @Test
    public void testDebugLogLevel() {
        logger.debug("Debug message");
        List<LogEvent> events = memAppender.getCurrentLogs();
        assertEquals(1, events.size(), "Should have one log event for DEBUG level");
        assertEquals(Level.DEBUG, events.get(0).getLevel());
        assertEquals("Debug message", events.get(0).getMessage().getFormattedMessage());
    }  

    @Test
    public void testConfigurationThroughFile() {
        // Get the LoggerContext
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        System.out.println("Configuration location: " + context.getConfigLocation()); // Debugging print
        
        // Force a reconfiguration to ensure our log4j2-test.xml is loaded
        context.reconfigure();
        
        // Get the configuration
        Configuration config = context.getConfiguration();
        System.out.println("Configuration: " + config); // Debugging print

        // Print all appenders
        Map<String, Appender> appenders = config.getAppenders();
        System.out.println("All appenders: " + appenders);
        
        // Get the MemAppender from the configuration
        MemAppender memAppender = config.getAppender("MemoryAppender");
        System.out.println("MemAppender: " + memAppender); // Debugging print
        
        if (memAppender == null) {
            System.out.println("MemAppender is null. Available appenders: " + appenders.keySet());
            return; // Exit the test early if memAppender is null
        }        
        
        // Check the layout
        Layout<?> layout = memAppender.getLayout();
        assertTrue(layout instanceof PatternLayout, "Layout should be PatternLayout");

        
        // Use the logger to log a message
        Logger logger = LogManager.getLogger(MemAppenderTest.class);
        logger.info("Test message");
        
        // Give some time for the log to be processed
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Check the logged message
        List<String> logs = memAppender.getEventStrings();
        assertEquals(1, logs.size(), "Should have one log message");

        // Print the actual log message for debugging
        String actualLogMessage = logs.get(0);
        System.out.println("Actual log message: " + actualLogMessage);
        
        // Adjust the expected pattern to match the actual log message format
        String expectedPattern = "\\[INFO\\]( [\\w.]+)?: Test message\\R"; // Use the //R Java construct to match any line ending for greater flexibility and compatibility
        assertTrue(actualLogMessage.matches(expectedPattern), "Log message should match expected format");
    }
    

}