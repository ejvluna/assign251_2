package assign251_2;

// Import the necessary classes
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Date;

// Class to represent a Velocity layout
@Plugin(name = "VelocityLayout", category = "Core", elementType = "layout", printObject = true)
public class VelocityLayout extends AbstractStringLayout {

    // variable to store the pattern of the layout
    private String pattern;
    // variable to store the VelocityEngine instance
    private VelocityEngine velocityEngine;

    // Constructor to initialize the VelocityLayout
    protected VelocityLayout(Charset charset, String pattern) {
        super(charset);
        this.pattern = pattern;
        initializeVelocityEngine();
    }

    // Method to format the log event using the Velocity template
    private void initializeVelocityEngine() {
        velocityEngine = new VelocityEngine();
        velocityEngine.init();
    }

    // Factory method to create and return the VelocityLayout instance
    @PluginFactory
    public static VelocityLayout createLayout(
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
            @PluginAttribute("pattern") String pattern) {
        return new VelocityLayout(charset, pattern);
    }
   
    // Method to set the pattern of the layout
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    // Method to get the pattern of the layout
    public String getPattern() {
        return pattern;
    }

     @Override
    public String toSerializable(LogEvent event) {
        VelocityContext context = new VelocityContext();
        context.put("c", event.getLoggerName());
        context.put("d", new Date(event.getTimeMillis()));
        context.put("m", event.getMessage().getFormattedMessage());
        context.put("p", event.getLevel().toString());
        context.put("t", event.getThreadName());
        context.put("n", System.lineSeparator());

        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "VelocityLayout", pattern);
        return writer.toString();
    }
}