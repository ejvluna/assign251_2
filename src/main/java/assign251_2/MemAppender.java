package assign251_2; // Specify the package of the class

// Import the necessary classes from the log4j library
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
// Import other necessary classes
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Class to represent a memory appender
@Plugin(name = "MemAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class MemAppender extends AbstractAppender {

    // variable to store the singleton instance of MemAppender
    private static MemAppender instance; 

    // REQUIRED list to store the log events
    private List<LogEvent> logEvents = new ArrayList<>(); 
    // REQUIRED variable to store the maximum size of the log events
    private int maxSize = Integer.MAX_VALUE; 

    // REQUIRED variable to store the number of discarded log events
    private long discardedLogCount = 0; 

    // REQUIRED Constructor to initialize the MemAppender (supports dependency injection)
    protected MemAppender(String name, Filter filter, PatternLayout layout, List<LogEvent> logEventsList) {
        super(name, filter, layout);
        this.logEvents = logEventsList != null ? logEventsList : new ArrayList<>();
    }

    // Factory method to create and return the singleton instance of MemAppender
    @PluginFactory
    public static MemAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") PatternLayout layout,
            @PluginElement("Filter") final Filter filter) {
        if (instance == null) {
            instance = new MemAppender(name, filter, layout, null);
        }
        return instance;
    }

    /* 
    // REQUIRED Method to set the layout of the MemAppender
    public void setLayout(Layout<? extends Serializable> layout) {
        super.setLayout(layout);
        // PENDING IMPLEMENTATION
    }
    */  

    // Method to set the maximum size of the log events
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    // Method to append a log event to the list of log events
    @Override
    public void append(LogEvent event) {
        if (logEvents.size() >= maxSize) {
            logEvents.remove(0);
            discardedLogCount++;
        }
        logEvents.add(event.toImmutable());
    }

    // REQUIRED Method to retrieve the number of discarded log events
    public long getDiscardedLogCount() {
        return discardedLogCount;
    }

    // REQUIRED Method to retrieve a copy of the current logs
    public List<LogEvent> getCurrentLogs() {
        return new ArrayList<>(logEvents);
    }

    // REQUIRED Method to retrieve the log events as a list of strings
    public List<String> getEventStrings() {
    return logEvents.stream()
        .map(event -> getLayout().toSerializable(event).toString())
        .collect(Collectors.toList());
}
    // REQUIRED Method to print the log events
    public void printLogs() {
        logEvents.forEach(event -> System.out.println(getLayout().toSerializable(event)));
        logEvents.clear();
    }

     

}