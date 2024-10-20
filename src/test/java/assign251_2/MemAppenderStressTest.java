package assign251_2;

// Import the necessary classes
import org.apache.logging.log4j.core.Appender;
import java.io.File;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

// Class to implement stress testing for the MemAppender class
public class MemAppenderStressTest {

    // Variables to store the MemAppender instances
    private MemAppender memAppenderLinkedList;
    private MemAppender memAppenderArrayList;
    private ConsoleAppender consoleAppender;
    private FileAppender fileAppender;
    private PatternLayout patternLayout;
    private VelocityLayout velocityLayout;
    private Logger logger;
    //private Layout<? extends Serializable> layout;

    // === Section 0. Methods to set up the test environment

    // Method to set up the test environment before each test
    @BeforeEach
    public void setUp() throws IOException {

        // Encourage garbage collection before each test
        System.gc();

        // Initialize a PatternLayout using the builder
        patternLayout = PatternLayout.newBuilder()
        .withPattern("%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n")
        .build();

        // Initialize a VelocityLayout using the Velocity template
        velocityLayout = VelocityLayout.createLayout(StandardCharsets.UTF_8, "[$p] $c $d: $m$n");
        
        // Configure logger
        LoggerContext context = LoggerContext.getContext(false);
        logger = context.getLogger(MemAppenderStressTest.class.getName());
        logger.setLevel(Level.ALL);

        // Setup appenders with VelocityLayout by default (you can change to PatternLayout if needed)
        setupAppendersWithLayout(velocityLayout);

        // Enable JMX profiler
        enableJMXProfiler();
        
        // Add delay to keep the test running for a longer duration (for profiling) e.g. 10 seconds
        try {
            Thread.sleep(10000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    
    // === 1.  MemAppender Stress Tests

    // Stress Test to ensure that MemAppender can handle a large number of log events (@100000 events) in a reasonable time (@90 seconds)
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS) // set time out to avoid infinite loop
    public void basicStressTest() {
        // Set the number of events to generate
        int eventCount = 100000; // Adjust this value as needed
        long startTime = System.currentTimeMillis();
        // Generate log events
        generateLogEvents(eventCount);
        // Measure the time taken to process the events
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        // Print the results to the console
        System.out.println("Time taken to process " + eventCount + " events: " + duration + " ms");
        // Add an assertion to verify the processing time meets the requirement (adjust as needed)
        assertTrue(duration < 90000, "Processing should take less than 90 seconds");
    }

    // Test to measure the performance of MemAppender before and after reaching the maxSize limit 
    @ParameterizedTest
    @ValueSource(ints = {1000, 10000}) // Adjust these values as needed
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    public void testPerformanceBeforeAndAfterMaxSize(int maxSize) {

        // Set the max size for the appenders
        memAppenderLinkedList.setMaxSize(maxSize);
        memAppenderArrayList.setMaxSize(maxSize);

        // Measure start time, mid time, and end time to calculate performance before and after reaching maxSize
        long startTime = System.nanoTime();
        long midTime = 0;
        long endTime;

        // Generate log events until reaching twice the maxSize (to measure performance for same maxSize events after reaching maxSize)
        for (int i = 0; i < 2 * maxSize; i++) {
            logger.info("Test message " + i);
            // Measure the mid time when reaching maxSize
            if (i == maxSize - 1) {
                midTime = System.nanoTime();
            }
        }
        // Measure the end time after reaching maxSize
        endTime = System.nanoTime();

        // Calculate the time taken to process events before and after reaching maxSize
        long durationBefore = midTime - startTime;
        long durationAfter = endTime - midTime;

        // Convert nanoseconds to milliseconds
        double durationBeforeMs = durationBefore / 1_000_000.0;
        double durationAfterMs = durationAfter / 1_000_000.0;

        // Print the results to the console
        System.out.println("MaxSize: " + maxSize);
        System.out.printf("Time taken to process %d events before reaching maxSize: %.2f ms%n", maxSize, durationBeforeMs);
        System.out.printf("Time taken to process %d events after reaching maxSize: %.2f ms%n", maxSize, durationAfterMs);
        System.out.println("Discarded log count (LinkedList): " + memAppenderLinkedList.getDiscardedLogCount());
        System.out.println("Discarded log count (ArrayList): " + memAppenderArrayList.getDiscardedLogCount());

        // Add assertions to verify expected performance (log discarding should be equal to maxSize)
        assertEquals(maxSize, memAppenderLinkedList.getDiscardedLogCount(), 
            "LinkedList should have discarded exactly maxSize logs");
        assertEquals(maxSize, memAppenderArrayList.getDiscardedLogCount(), 
            "ArrayList should have discarded exactly maxSize logs");
    }
    

    // --- 2. Appender Performance Comparison Test (processing time & memory consumption)

    // Test to compare the performance (processing time) of MemAppender (with different data structures), ConsoleAppender, and FileAppender
    @Test
    public void compareAppenderPerformance() {
        // Set the number of log events to generate
        int eventCount = 1000; // Increase event count as needed
        String testFileName = "test_output.log";

        // Configure the FileAppender with a test file
        FileAppender fileAppender = FileAppender.newBuilder()
            .setName("TestFileAppender")
            .withFileName(testFileName)
            .setLayout(patternLayout)
            .build();
        fileAppender.start();

        // Run performance test for each appender type
        System.out.println("Starting MemAppender (LinkedList) test");
        runAppenderTest(memAppenderLinkedList, eventCount);

        System.out.println("Starting MemAppender (ArrayList) test");
        runAppenderTest(memAppenderArrayList, eventCount);

        System.out.println("Starting ConsoleAppender test");
        runAppenderTest(consoleAppender, eventCount);

        System.out.println("Starting FileAppender test");
        runAppenderTest(fileAppender, eventCount);

        // Clean up: stop the file appender and delete the test file
        fileAppender.stop();
        new File(testFileName).delete();

        System.out.println("Performance tests completed. Please analyze results in VisualVM.");
    }

    // Helper method to run the appender test
    private void runAppenderTest(Appender appender, int eventCount) {
        // Remove existing appenders and add the specified appender
        logger.getAppenders().forEach((name, app) -> logger.removeAppender(app));
        logger.addAppender(appender);
        
        // Measure memory consumption and time before generating log events
        System.gc(); // Encourage garbage collection before measurement
        long startMemory = getMemoryUsage();
        long startTime = System.currentTimeMillis();
        
        // Generate log events
        generateLogEvents(eventCount);
        
        // Measure memory consumption and time after generating log events
        long endTime = System.currentTimeMillis();
        System.gc(); // Encourage garbage collection after measurement
        long endMemory = getMemoryUsage();
        
        // Calculate time taken and memory used
        long duration = endTime - startTime;
        long memoryUsed = endMemory - startMemory;
        
        // Remove the appender
        logger.removeAppender(appender);

        // Print the results to the console
        System.out.println(appender.getName() + " processed " + eventCount + " events in " + duration + " ms");
        System.out.println("Memory used: " + memoryUsed + " bytes");
        
        // Print additional information for MemAppender (discarded log count)
        if (appender instanceof MemAppender) {
            MemAppender memAppender = (MemAppender) appender;
            System.out.println("  Discarded log count: " + memAppender.getDiscardedLogCount());
        }
    }

    // Helper method to measure the memory usage
    private long getMemoryUsage() {
        // Get the current memory usage
        Runtime runtime = Runtime.getRuntime();
        // Calculate the memory used
        return runtime.totalMemory() - runtime.freeMemory();
    }


    // --- 3. Layout Performance Comparison Test (processing time & memory consumption)

    // Test to compare the performance (processing time & memory usage) of PatternLayout and VelocityLayout
    @Test
    public void compareLayoutPerformance() {
        // Set the number of log events to generate
        int eventCount = 1000; // Adjust this value as needed

        // Test PatternLayout performance and memory usage
        switchToPatternLayout();
        System.gc(); // Encourage garbage collection before measurement
        long patternStartMemory = getMemoryUsage();
        long patternStartTime = System.currentTimeMillis();
        generateLogEvents(eventCount);
        long patternEndTime = System.currentTimeMillis();
        System.gc(); // Encourage garbage collection after measurement
        long patternEndMemory = getMemoryUsage();
        long patternDuration = patternEndTime - patternStartTime;
        long patternMemoryUsed = patternEndMemory - patternStartMemory;

        // Test VelocityLayout performance and memory usage
        switchToVelocityLayout();
        System.gc(); // Encourage garbage collection before measurement
        // Measure memory consumption before generating log events
        long velocityStartMemory = getMemoryUsage();
        long velocityStartTime = System.currentTimeMillis();
        generateLogEvents(eventCount);
        // Measure memory consumption after generating log events
        long velocityEndTime = System.currentTimeMillis();
        System.gc(); // Encourage garbage collection after measurement
        long velocityEndMemory = getMemoryUsage();
        long velocityDuration = velocityEndTime - velocityStartTime;
        long velocityMemoryUsed = velocityEndMemory - velocityStartMemory;

        // Print results
        System.out.println("PatternLayout - Time: " + patternDuration + " ms, Memory: " + patternMemoryUsed + " bytes");
        System.out.println("VelocityLayout - Time: " + velocityDuration + " ms, Memory: " + velocityMemoryUsed + " bytes");

        // Calculate and print performance improvements
        double speedImprovement = ((double)(velocityDuration - patternDuration) / velocityDuration) * 100;
        double memoryImprovement = ((double)(velocityMemoryUsed - patternMemoryUsed) / velocityMemoryUsed) * 100;

        // Compare performance
        if (patternDuration < velocityDuration) {
            System.out.println("PatternLayout was faster by " + (velocityDuration - patternDuration) + " ms");
            System.out.printf("PatternLayout is %.2f%% faster than VelocityLayout\n", speedImprovement);
        } else if (velocityDuration < patternDuration) {
            System.out.println("VelocityLayout was faster by " + (patternDuration - velocityDuration) + " ms");
        } else {
            System.out.println("Both layouts performed equally in terms of time");
        }

        // Compare memory usage
        if (patternMemoryUsed < velocityMemoryUsed) {
            System.out.println("PatternLayout used less memory by " + (velocityMemoryUsed - patternMemoryUsed) + " bytes");
            System.out.printf("PatternLayout uses %.2f%% less memory than VelocityLayout\n", memoryImprovement);
        } else if (velocityMemoryUsed < patternMemoryUsed) {
            System.out.println("VelocityLayout used less memory by " + (patternMemoryUsed - velocityMemoryUsed) + " bytes");
        } else {
            System.out.println("Both layouts used the same amount of memory");
        }
    }


    // === Additional Helper Methods

    // Method to enable JMX profiler for monitoring the application via JConsole or VisualVM
    private void enableJMXProfiler() {
        try {
            java.lang.management.ManagementFactory.getPlatformMBeanServer();
            java.lang.management.RuntimeMXBean runtime = 
                java.lang.management.ManagementFactory.getRuntimeMXBean();
            String jvmName = runtime.getName();
            System.out.println("JVM Name = " + jvmName);
            long pid = Long.valueOf(jvmName.split("@")[0]);
            System.out.println("JVM PID  = " + pid);
        } catch (Exception e) {
            System.out.println("Error setting up JMX profiler: " + e.getMessage());
        }
    }

    private void setupAppendersWithLayout(Layout<? extends Serializable> layout) {
        // Remove existing appenders
        logger.getAppenders().forEach((name, appender) -> logger.removeAppender(appender));
    
        // Create new appenders with the specified layout
        memAppenderLinkedList = MemAppender.createAppenderForStressTest(
            "MemAppenderLinkedList",
            layout,
            new LinkedList<>()
        );
        memAppenderArrayList = MemAppender.createAppenderForStressTest(
            "MemAppenderArrayList",
            layout,
            new ArrayList<>()
        );
        consoleAppender = ConsoleAppender.newBuilder()
            .setName("ConsoleAppender")
            .setLayout(layout)
            .build();
        fileAppender = FileAppender.newBuilder()
            .setName("FileAppender")
            .withFileName("stress_test_logs.log")
            .setLayout(layout)
            .build();
    
        // Start appenders
        memAppenderLinkedList.start();
        memAppenderArrayList.start();
        consoleAppender.start();
        fileAppender.start();
    
        // Add appenders to logger
        logger.addAppender(memAppenderLinkedList);
        logger.addAppender(memAppenderArrayList);
        logger.addAppender(consoleAppender);
        logger.addAppender(fileAppender);
    }

    private void switchToPatternLayout() {
        setupAppendersWithLayout(patternLayout);
    }
    
    private void switchToVelocityLayout() {
        setupAppendersWithLayout(velocityLayout);
    }

    // Utility method for generating logs, measuring performance, etc. will be added here
    private void generateLogEvents(int count) {
        for (int i = 0; i < count; i++) {
            LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Test message " + i))
                .build();
            // Extract information from the log event and use it to log
            logger.log(event.getLevel(), event.getMessage());
        }
    }

}


